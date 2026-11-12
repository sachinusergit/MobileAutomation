# MobileAutomation — working rules for the Meesho Supply suite

These are the agreed rules for **every** change in this repo. They are not open
for interpretation: follow them exactly, do not ask for a reminder, and keep this
file up to date when a rule changes.

## 1. How to run

```cmd
cd /d C:\Users\sachi\IdeaProjects\MobileAutomation
set "JAVA_HOME=%USERPROFILE%\.jdks\openjdk-27"
mvnw.cmd -B test -Pall-tests
```

- `-Pall-tests` = everything, `-Pandroid` = app-launch/launch/signup/categories,
  `-Pe2e` = the purchase (pricing/payment) journeys.
- A single feature: `mvnw.cmd -B test -Pandroid -Dcucumber.filter.tags="@categories"`.
- Faster without video: add `-Dvideo.recording.enabled=false`.

## 2. Global rules

- The app is **never closed, force-stopped or relaunched** once it runs. Only
  `mobile: activateApp` / `mobile: startActivity` bring it forward.
- The Appium Settings helper is never installed per session
  (`app.skipDeviceInitialization=true`).
- No Settings-app tests exist (the Settings steps are iOS only).
- **No unnecessary waits or sleeps.** Poll for elements; keep sleeps under
  300 ms and only where an animation forces it. Execution must stay fast.
- When an element is not found, **scroll the page up and down** to look for it
  (`BasePage.findByScrolling`) instead of failing immediately.
- When an element is not clickable, find it again and **force the click/tap** —
  never restart the scenario from the first step.
- When **any step fails**: identify that step, go **one page back**, reach the
  **same page** again and **execute the same step** once more (up to 3 tries).
  Never restart the scenario, never log out, never log in again, never jump to
  another section.
- The **account tab is only used by the sign-up feature**. The categories and
  purchase flows go **directly to Categories** — never through the Home tab.
- When Meesho shows its pop-ups, **answer them with a value**: language →
  **English**, gender → **Male**. Never just close them.
- **Never touch the device (adb) while a run is in progress** — it kills the
  UiAutomator2 instrumentation.
- Device: OPPO A54 (CPH2239, API 30, 720x1600). Its wireless-ADB serial
  changes on every reconnect, so the framework resolves the device that
  `adb devices` reports and reconnects it automatically.

## 3. Product details page — the fixed order

1. **Open the product from the listing — the 7th index card** of the rendered
   cards. Never scroll the listing more than needed, never open another card
   (if that card has no pictures, check the 8th/9th card instead).
2. **Open the full-screen gallery and scroll the images 3 times to the right
   and 1 time to the left** (gentle, centred drag). A product with a single
   picture keeps its one image — we do not go back to the listing for another
   product.
3. **Scroll the page until the size chips are found** (scroll the details page
   where the images are shown).
4. **Select the size — mandatory, never skipped:**
   - fetch the sizes the product offers;
   - if the mentioned size **32 is not available, select ANY one of the available
     size cards and move on** — do not stop, do not fail, do not go back to the
     listing;
   - if the chip does not react, **force the tap** on the chip's centre;
   - then **click Buy Now**.
   - indirect way: when the size was not selected on the details page, Buy Now
     opens the **select preferences** sheet — pick a size there (forced).
5. **Verify the price with the circled "i" button** next to the price on the
   details page (`price_icon_info`): click it, verify **each** detail — MRP,
   product price, discount, final price — then click **OK** to close the
   dialog and match the captured final price against the price shown on the
   product page.
6. **Continue with the remaining elements** (do not stop on the details page):
   - click **Buy Now** (the PDP CTA) — when the size was chosen on the details
     page the order may skip the preferences sheet, otherwise pick the size in
     that sheet;
   - verify the **Review your order** page — the delivery address and that the
     three rupee prices are equal (`offerPrice`, `amount_title`,
     `multi_cta_info_overlapping_text`);
   - click **Continue** on the review page;
   - verify the **Payment Method** page — Cash on Delivery and Pay Online
     options with their prices.

## 4. Prices

- The **payable** price is the one in the select-preferences sheet; the three
  rupee amounts on the review page must all equal it:
  `offerPrice` (product row), `amount_title` (price details),
  `multi_cta_info_overlapping_text` (sticky footer).
- The product page price can be lower than the payable price (Meesho adds a
  location discount there), so the sheet price is compared with the **review
  page** price, not with the product page price.
- Payment page: read Cash on Delivery and Pay Online from the `price_tv` on the
  same row as each option title.

## 5. Where the execution stands (always track it)

Every step logs a progress line:

```
PROGRESS PAGE [product details page] | VERIFIED [categories screen, category rail, ...] | PENDING [review order page and its three prices]
```

- The **current page** and the **verifications that are done / still open** are
  tracked in `VerificationTracker` and printed after every step.
- The check list of the journey (in order): app launched → categories screen →
  category rail → men / bottom wear → cargos/trousers listing → product details
  page → full screen gallery and image scroll → select size section and
  selected size → circled "i" price details → buy now and select preferences
  sheet → review order page and its three prices → payment method page.
- **Never get stuck in the middle of the product details page.** When a step
  fails, continue from the page that is on top: go one page back, reach the
  same page and verify the remaining elements from there.
- A dead session (the wireless device dropped) is replaced by a new one and
  **the same step is repeated** — the scenario is never restarted.

## 6. Home page filter panel — the fixed order

1. **Verify the footer Home tab is active** (`action_first_menu` has
   `selected="true"`).
2. Tap the **Filters** option at the far right of the filter row, directly
   below the search bar.
3. The bottom sheet shows the filter sections on the **left rail** and the
   options as tappable **pills** on the right.
4. **Apply the filters in the order that actually works on this app:**
   Gender → Discount (50% And Above) → Price (Under ₹499) → Color. The server
   hides the Discount section while a colour is selected, so the Discount has
   to be selected **before** the colour.
5. **Select the colour that is offered**: if Orange is available select it,
   otherwise select whatever colour is shown (read the pills and pick the
   first one).
6. Confirm with the **Done** button at the bottom-right of the sheet.
7. The selected pill carries `selected="true"` — always probe it instead of
   guessing. **Never tap a pill that is already selected** — tapping again
   would unselect it. Select only when `selected="false"`; when it is already
   `true`, just log it and move on.
8. **Log every selection.** Each pill tap logs `Filter selected -> <Section>
   = <value>` (or `Filter already selected -> ...` when it was already on), and
   the Done tap logs the full summary
   `Applied filters: Gender = Men | Discount = 50% And Above | Price = Under
   ₹ 499 | Color = Black`.
9. `UiScrollable(..., setAsVerticalList()).scrollIntoView(text(section))` is the
   reliable way to reach a section on the rail; the plain `text(...)` lookup
   drops off-screen labels after other sections get selected.

## 7. Home search bar — the fixed order

1. Verify the footer **Home** tab is the active tab (`action_first_menu` has
   `selected="true"`).
2. Tap the **search bar** at the top of the home page (`query_edit_text`).
3. The field expands and shows **recent / popular searches**; clear the field
   and type the query (`Men shirts`).
4. Read the **autosuggestions** as a list and log them
   (`list_item_title` rows).
5. **Select the 5th suggestion** — rows are zero-indexed in code, so index 4.
   On this build the 5th suggestion is `shirt for men under 200`.
6. The results page shows the products for the selected suggestion
   (`item_catalog_card_optimised`).

## 8. Feature files (the source of truth)

- `01-app-launch.feature`, `02-meesho-launch.feature`, `03-meesho-signup.feature`
- `04-meesho-categories.feature` — the complete order flow: catalogue → Men →
  Bottom Wear → Cargos/Trousers → product 7 → gallery → image scroll → size
  section → size → circled "i" price details → Buy Now → review page (three
  prices) → Continue → payment method page (COD / Pay Online).
- `meesho-purchase.feature` — pricing/payment, two scenarios: size selected on
  the details page, and size selected in the select preferences sheet.
- `05-meesho-filters.feature` — home page filter panel: footer Home active →
  Filters → Gender = Men → Discount = 50% And Above → Price = Under ₹499 →
  Color (available one) → Done. Filter selections are logged per pill and as a
  final `Applied filters: ...` summary.
- `06-meesho-search.feature` — home search bar: footer Home active → tap the
  search bar → type `Men shirts` → read the autosuggestions → select the 5th
  suggestion (`shirt for men under 200`) → the results page displays the
  products for the selected suggestion.
- Steps are written **exactly** as the feature file says. The product number is
  a parameter (`product number {int}`), so the file decides which card is
  clicked.
- Each step belongs to one feature; no duplicated step blocks between features.
