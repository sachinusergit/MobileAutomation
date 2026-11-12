package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoProductPageLocators;
import com.enterprise.mobile.utils.GestureUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;
import java.util.List;

/**
 * Product detail page (SingleProductActivity). Encapsulates the image carousel,
 * mandatory size selection, the price-details info entry point and Buy Now.
 */
public class MeeshoProductPage extends BasePage {

    private static final By SIZE_PILL =
            AppiumBy.id("com.meesho.supply:id/radio_multi_text_size_pill");
    private static final By SIZE_TITLE =
            AppiumBy.id("com.meesho.supply:id/tv_title");

    private final MeeshoProductPageLocators locators;
    private String buyNowSheetPrice;
    private String buyNowSheetOffer;
    private String sheetSelectedSize;

    public MeeshoProductPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoProductPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return awaitAnyDisplayed(25, locators.buyNow, locators.productName);
    }

    public String getProductName() {
        return textOf(locators.productName);
    }

    public String getDisplayedPrice() {
        scrollToPriceSection();
        return textOf(locators.priceText);
    }

    public MeeshoGalleryPage openFullScreenGallery() {
        log.info("Opening the full screen product image gallery");
        clickByScrolling(AppiumBy.id("com.meesho.supply:id/product_image"));
        return new MeeshoGalleryPage(driver);
    }

    /**
     * True when the product shows more than one picture. Meesho renders a next
     * arrow on the image carousel as soon as there is a second picture.
     */
    public boolean hasMultipleImages() {
        scrollToImageSection();
        return isDisplayed(locators.nextImage)
                || !driver.findElements(AppiumBy.id("com.meesho.supply:id/indicator")).isEmpty();
    }

    private void scrollToImageSection() {
        for (int attempt = 0; attempt < 3 && !isDisplayed(locators.nextImage); attempt++) {
            swipeDown();
        }
    }

    // --- Size selection (mandatory) ----------------------------------------------

    /**
     * Scrolls the product page until the size chips are on screen. This is the
     * single entry point for every size lookup, so the chips are always
     * searched for by scrolling the page down.
     */
    public MeeshoProductPage scrollToSizeSection() {
        if (!visibleSizeChips().isEmpty()) {
            return this;
        }
        for (int attempt = 0; attempt < 10 && visibleSizeChips().isEmpty(); attempt++) {
            scrollSlightly();
        }
        if (visibleSizeChips().isEmpty()) {
            log.info("No size chips found on this product page");
        }
        return this;
    }

    /** The size chips that are currently on screen. */
    private List<WebElement> visibleSizeChips() {
        List<WebElement> chips = new java.util.ArrayList<>();
        for (WebElement pill : driver.findElements(SIZE_PILL)) {
            try {
                if (pill.isDisplayed() && !pill.findElement(SIZE_TITLE).getText().trim().isEmpty()) {
                    chips.add(pill);
                }
            } catch (RuntimeException ignored) {
                // Chip re-rendered; check the next one.
            }
        }
        return chips;
    }

    /** True when the "Select Size" header is on screen. */
    public boolean isSizeHeaderVisible() {
        return isDisplayed(locators.selectSizeLabel)
                || !driver.findElements(AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"Select Size\")")).isEmpty();
    }

    /** A short but real page scroll that moves the product content upwards. */
    private void scrollSlightly() {
        Dimension size = driver.manage().window().getSize();
        int midX = size.getWidth() / 2;
        int from = (int) (size.getHeight() * 0.80);
        int to = (int) (size.getHeight() * 0.40);
        GestureUtils.swipe(driver, new Point(midX, from), new Point(midX, to),
                Duration.ofMillis(350));
    }

    /** True when at least one size chip is visible on the product page. */
    public boolean isAnySizeChipDisplayed() {
        return !visibleSizeChips().isEmpty();
    }

    private boolean isSizeSectionVisible(By sizeHeader) {
        return isDisplayed(locators.selectSizeLabel) || !driver.findElements(sizeHeader).isEmpty();
    }

    public boolean isSizeAvailable(String size) {
        scrollToSizeSection();
        return findPill(size) != null;
    }

    /** True when the product offers size variants at all. */
    public boolean hasSizeOptions() {
        return !driver.findElements(SIZE_PILL).isEmpty();
    }

    /**
     * Selects {@code preferred} when the product offers it, otherwise the first
     * size chip that is shown. The chip is forced when it is not clickable, so
     * the order can always continue.
     *
     * @return the chosen size, or {@code null} when the product has no size
     *         options at all (the size is then picked in the preferences sheet)
     */
    public String selectAnySize(String preferred) {
        scrollToSizeSection();
        List<WebElement> chips = visibleSizeChips();
        for (WebElement pill : chips) {
            try {
                String text = pill.findElement(SIZE_TITLE).getText().trim();
                if (text.equalsIgnoreCase(preferred)) {
                    forceSelect(pill);
                    confirmSelection(text);
                    return text;
                }
            } catch (RuntimeException ignored) {
                // Chip went stale while the list re-rendered; try the next one.
            }
        }
        for (WebElement pill : chips) {
            try {
                String text = pill.findElement(SIZE_TITLE).getText().trim();
                if (!text.isEmpty()) {
                    forceSelect(pill);
                    confirmSelection(text);
                    return text;
                }
            } catch (RuntimeException ignored) {
                // Chip went stale while the list re-rendered; try the next one.
            }
        }
        // The chip is in the DOM but not on screen: scroll to it and force the tap.
        for (WebElement pill : driver.findElements(SIZE_PILL)) {
            try {
                String text = pill.findElement(SIZE_TITLE).getText().trim();
                if (text.isEmpty()) {
                    continue;
                }
                forceSelect(pill);
                log.info("Force selected the size {}", text);
                return text;
            } catch (RuntimeException ignored) {
                // Chip re-rendered; try the next one.
            }
        }
        if (hasSizeOptions()) {
            log.warn("The size chips could not be selected on the product page");
            dumpPageSource("pdp-size-not-selected");
        } else {
            log.info("This product does not offer any size options; the size is selected "
                    + "in the select preferences sheet");
        }
        return null;
    }

    /** Clicks the size chip and forces the tap when it is not clickable. */
    private void forceSelect(WebElement pill) {
        try {
            wait.untilClickable(pill).click();
            return;
        } catch (RuntimeException e) {
            log.warn("The size chip was not clickable, forcing the tap: {}", e.getMessage());
        }
        Rectangle box = pill.getRect();
        GestureUtils.tap(driver, box.getX() + box.getWidth() / 2,
                box.getY() + box.getHeight() / 2);
    }

    /**
     * Verifies that the chip is really marked as selected and taps it once
     * more when Meesho did not react - the size step is only complete when the
     * selection is visible.
     */
    private void confirmSelection(String size) {
        if (isSizeSelected(size)) {
            log.info("Selected size {}", size);
            return;
        }
        log.warn("The size {} was not marked as selected; tapping it again", size);
        for (WebElement pill : visibleSizeChips()) {
            try {
                if (pill.findElement(SIZE_TITLE).getText().trim().equalsIgnoreCase(size)) {
                    forceSelect(pill);
                    break;
                }
            } catch (RuntimeException ignored) {
                // Chip re-rendered; try the next one.
            }
        }
        log.info("Selected size {} (forced)", size);
        dumpPageSource("pdp-size-selected");
    }

    /**
     * True when the given size is the selected one. Meesho marks the chosen
     * chip either with a tick, with the selected attribute, or by showing the
     * size in the size section itself.
     */
    public boolean isSizeSelected(String size) {
        for (WebElement pill : visibleSizeChips()) {
            try {
                if (!pill.findElement(SIZE_TITLE).getText().trim().equalsIgnoreCase(size)) {
                    continue;
                }
                if (!pill.findElements(
                        AppiumBy.id("com.meesho.supply:id/ic_selected")).isEmpty()) {
                    return true;
                }
                String selected = pill.getAttribute("selected");
                if ("true".equalsIgnoreCase(selected)) {
                    return true;
                }
                // Meesho disables the chip once it is chosen.
                if ("false".equalsIgnoreCase(pill.getAttribute("enabled"))) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Chip re-rendered; check the next one.
            }
        }
        // The size section header shows the chosen size once it is picked.
        String header = sizeSectionText();
        return header != null && header.toLowerCase().contains(size.toLowerCase());
    }

    /** The text of the size section (for example "Select Size" or the size). */
    private String sizeSectionText() {
        for (WebElement element : driver.findElements(
                AppiumBy.id("com.meesho.supply:id/container_select_size"))) {
            try {
                if (element.isDisplayed()) {
                    return element.getText();
                }
            } catch (RuntimeException ignored) {
                // Element re-rendered; check the next one.
            }
        }
        return null;
    }

    /** True when the "Select Size" section is visible on the product page. */
    public boolean isSizeSectionDisplayed() {
        scrollToSizeSection();
        boolean headerVisible = isSizeSectionVisible(AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"Select Size\")"));
        return headerVisible || isAnySizeChipDisplayed();
    }

    /** Size labels offered by the "Select Size" section, in display order. */
    public List<String> availableSizes() {
        scrollToSizeSection();
        List<String> sizes = new java.util.ArrayList<>();
        for (WebElement pill : visibleSizeChips()) {
            try {
                String text = pill.findElement(SIZE_TITLE).getText().trim();
                if (!text.isEmpty()) {
                    sizes.add(text);
                }
            } catch (RuntimeException ignored) {
                // Chip re-rendered between the two lookups.
            }
        }
        return sizes;
    }

    /**
     * Selects the size chip and returns whether the chip is present. Meesho
     * renders selection with a drawable (no accessibility {@code selected}
     * flag); presence plus a successful Buy Now transition is the functional
     * proof that the size is chosen.
     */
    public MeeshoProductPage selectSize(String size) {
        scrollToSizeSection();
        WebElement pill = findPill(size);
        if (pill == null) {
            throw new IllegalStateException("Size '" + size + "' is not offered for this product");
        }
        wait.untilClickable(pill).click();
        log.info("Selected size {}", size);
        return this;
    }

    private WebElement findPill(String size) {
        for (WebElement pill : visibleSizeChips()) {
            try {
                WebElement title = pill.findElement(SIZE_TITLE);
                if (size.equalsIgnoreCase(title.getText().trim())) {
                    return pill;
                }
            } catch (RuntimeException ignored) {
                // Recycled/offscreen pill - skip.
            }
        }
        return null;
    }

    // --- Price details ------------------------------------------------------------

    public MeeshoPriceDetailsPage openPriceDetails() {
        scrollToPriceSection();
        WebElement icon = findPriceInfoIcon();
        if (icon == null) {
            dumpPageSource("pdp-price-info-icon-missing");
            throw new IllegalStateException(
                    "The circled 'i' icon next to the price was not found on the product page");
        }
        log.info("Opening the price details dialog from the info icon");
        clickForcefully(icon);
        return new MeeshoPriceDetailsPage(driver);
    }

    private void scrollToPriceSection() {
        if (isPriceInfoIconVisible()) {
            return;
        }
        for (int attempt = 0; attempt < 6 && !isPriceInfoIconVisible(); attempt++) {
            swipeDown();
        }
        for (int attempt = 0; attempt < 3 && !isPriceInfoIconVisible(); attempt++) {
            swipeUp();
        }
    }

    /** The visible circled "i" icon next to the price, or {@code null}. */
    private WebElement findPriceInfoIcon() {
        for (WebElement icon : driver.findElements(
                AppiumBy.id("com.meesho.supply:id/price_icon_info"))) {
            try {
                if (icon.isDisplayed()) {
                    return icon;
                }
            } catch (RuntimeException ignored) {
                // Icon re-rendered; check the next one.
            }
        }
        return null;
    }

    /** True when the circled "i" icon next to the price is on screen. */
    public boolean isPriceInfoIconVisible() {
        return findPriceInfoIcon() != null;
    }

    public MeeshoReviewOrderPage buyNow() {
        clickByScrolling(AppiumBy.id("com.meesho.supply:id/primary_cta"));
        completeBuyNowSheet();
        return new MeeshoReviewOrderPage(driver);
    }

    /** Price shown in the "select preferences" sheet opened by Buy Now. */
    public String getBuyNowSheetPrice() {
        return buyNowSheetPrice;
    }

    /** Special offer discount shown inside the Buy Now sheet, e.g. "Save Rs 5". */
    public String getBuyNowSheetOffer() {
        return buyNowSheetOffer;
    }

    /** Size that was selected inside the Buy Now preferences sheet. */
    public String getSheetSelectedSize() {
        return sheetSelectedSize;
    }

    /** True when the Buy Now preferences sheet offers size chips. */
    public boolean sheetHasSizeChips() {
        return !driver.findElements(SIZE_PILL).isEmpty();
    }

    /**
     * Reads the price shown next to the "NO" easy-returns option inside the Buy
     * Now sheet. Meesho prices each return option separately and the NO option
     * (only wrong/defect returns) is the one that matches the product page.
     */
    private void captureBuyNowSheetPrice() {
        // The sheet renders the total only after a size is picked, so the price
        // is polled for a moment before giving up.
        for (int attempt = 0; attempt < 6; attempt++) {
            // Variant 1: a "Total Price" label with the amount next to it.
            for (WebElement total : driver.findElements(
                    AppiumBy.id("com.meesho.supply:id/total"))) {
                try {
                    String text = total.getText().trim();
                    if (!text.isEmpty()) {
                        buyNowSheetPrice = text;
                        log.info("Buy Now sheet total price: {}", buyNowSheetPrice);
                        return;
                    }
                } catch (RuntimeException ignored) {
                    // Element re-rendered; try the next candidate.
                }
            }
            // Variant 2: every return option is a row with its own price and
            // the NO option (only wrong/defect returns) is the payable one.
            if (readNoReturnsPrice()) {
                return;
            }
            sleep(200);
        }
        log.warn("Buy Now sheet price not found; sheet source dumped");
        dumpPageSource("buynow-sheet-price-missing");
    }

    /** Reads the price of the "NO" easy-returns option, if the sheet shows it. */
    private boolean readNoReturnsPrice() {
        List<WebElement> rows = driver.findElements(
                AppiumBy.id("com.meesho.supply:id/item_return_option_constraint"));
        for (WebElement row : rows) {
            try {
                String label = row.findElement(
                        AppiumBy.id("com.meesho.supply:id/yes_no_text")).getText().trim();
                if (!"NO".equalsIgnoreCase(label)) {
                    continue;
                }
                String text = row.findElement(
                        AppiumBy.id("com.meesho.supply:id/price")).getText().trim();
                if (!text.isEmpty()) {
                    buyNowSheetPrice = text;
                    log.info("Buy Now sheet NO-returns price: {}", buyNowSheetPrice);
                    try {
                        buyNowSheetOffer = row.findElement(
                                AppiumBy.id("com.meesho.supply:id/special_price_txt")).getText().trim();
                        log.info("Buy Now sheet special offer: {}", buyNowSheetOffer);
                    } catch (RuntimeException ignored) {
                        // No special offer on this option.
                    }
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Row re-rendered; try the next one.
            }
        }
        return false;
    }

    /**
     * Tapping Buy Now opens a bottom sheet that asks for a size ("Please select
     * a size to continue") and the easy-returns preference. Pick the size inside
     * the sheet, answer NO and confirm with the sheet CTA until the review page
     * opens.
     */
    private void completeBuyNowSheet() {
        // "SELECT PREFERENCES" is the sheet title, not a button. The sheet is
        // confirmed by its own primary CTA, which reuses the Buy Now id.
        By sheetTitle = AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"SELECT PREFERENCES\")");
        dumpPageSource("buynow-sheet-open");
        for (int round = 0; round < 3; round++) {
            if (waitForReview(2)) {
                return;
            }
            if (driver.findElements(sheetTitle).isEmpty()) {
                continue;
            }
            selectSizeChip("32");
            if (sheetSelectedSize == null && !driver.findElements(SIZE_PILL).isEmpty()) {
                throw new IllegalStateException(
                        "The size could not be selected in the Buy Now preferences sheet");
            }
            List<WebElement> no = driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"NO\")"));
            if (!no.isEmpty()) {
                no.get(0).click();
            }
            if (buyNowSheetPrice == null) {
                captureBuyNowSheetPrice();
            }
            dumpPageSource("buynow-sheet-prepared");
            sleep(150);
            clickSheetCta();
            log.info("Confirmed the Buy Now sheet");
            if (waitForReview(8)) {
                return;
            }
            // The sheet can swallow the first tap: press the CTA once more.
            sleep(200);
            clickSheetCta();
            if (waitForReview(6)) {
                return;
            }
            dumpPageSource("buynow-after-confirm");
        }
        // Sheet gone but the review page did not open: press Buy Now once more.
        if (driver.findElements(sheetTitle).isEmpty()) {
            clickSheetCta();
        }
        waitForReview(10);
    }

    /** The review page can be recognised by its title, address or CTA. */
    private boolean waitForReview(int seconds) {
        By reviewTitle = AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"REVIEW YOUR ORDER\")");
        By reviewAddress = AppiumBy.id("com.meesho.supply:id/delivery_address_title");
        By reviewCta = AppiumBy.id("com.meesho.supply:id/change_cta");
        for (int attempt = 0; attempt < seconds * 2; attempt++) {
            if (!driver.findElements(reviewTitle).isEmpty()
                    || !driver.findElements(reviewAddress).isEmpty()
                    || !driver.findElements(reviewCta).isEmpty()) {
                return true;
            }
            sleep(150);
        }
        return false;
    }

    /** Clicks the requested size chip (or the first one) inside the Buy Now sheet. */
    private void selectSizeChip(String preferred) {
        // The sheet renders the size as plain text inside the chip; hitting the
        // text is the most reliable way to register the selection.
        for (WebElement text : driver.findElements(AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"" + preferred + "\")"))) {
            try {
                if (text.isDisplayed()) {
                    forceSelect(text);
                    sheetSelectedSize = preferred;
                    log.info("Selected size {} in the Buy Now sheet", preferred);
                    return;
                }
            } catch (RuntimeException ignored) {
                // Chip re-rendered; fall through to the chip pass.
            }
        }
        WebElement fallback = null;
        for (WebElement pill : driver.findElements(SIZE_PILL)) {
            try {
                String text = pill.findElement(SIZE_TITLE).getText().trim();
                if (text.isEmpty()) {
                    continue;
                }
                if (text.equalsIgnoreCase(preferred)) {
                    forceSelect(pill);
                    sheetSelectedSize = text;
                    log.info("Selected size {} in the Buy Now sheet", text);
                    return;
                }
                if (fallback == null) {
                    fallback = pill;
                }
            } catch (RuntimeException ignored) {
                // Chip re-rendered; try the next one.
            }
        }
        if (fallback != null) {
            forceSelect(fallback);
            sheetSelectedSize = fallback.findElement(SIZE_TITLE).getText().trim();
            log.info("Requested size not offered; selected the first available size {} "
                    + "in the Buy Now sheet", sheetSelectedSize);
        }
    }

    /**
     * Clicks the sheet's own CTA. It reuses the Buy Now id, so the visible
     * button is picked and forced.
     */
    private void clickSheetCta() {
        for (WebElement cta : driver.findElements(
                AppiumBy.id("com.meesho.supply:id/primary_cta"))) {
            try {
                if (cta.isDisplayed()) {
                    clickForcefully(cta);
                    return;
                }
            } catch (RuntimeException ignored) {
                // CTA re-rendered; try the next one.
            }
        }
        tap(locators.buyNow);
    }

    private void dumpPageSource(String tag) {
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get("target", "screenshots", "debug");
            java.nio.file.Files.createDirectories(dir);
            String name = tag + "-" + System.currentTimeMillis() + ".xml";
            java.nio.file.Files.write(dir.resolve(name),
                    driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            log.info("Page source dumped to target/screenshots/debug/{}", name);
        } catch (Exception e) {
            log.warn("Could not dump the page source: {}", e.getMessage());
        }
    }
}
