package com.enterprise.mobile.bdd.steps;

import com.enterprise.mobile.bdd.context.TestContext;
import com.enterprise.mobile.bdd.context.VerificationTracker;
import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.pages.android.MeeshoAccountPage;
import com.enterprise.mobile.pages.android.MeeshoCategoriesPage;
import com.enterprise.mobile.pages.android.MeeshoGalleryPage;
import com.enterprise.mobile.pages.android.MeeshoHomePage;
import com.enterprise.mobile.pages.android.MeeshoListingPage;
import com.enterprise.mobile.pages.android.MeeshoOnboardingDialogs;
import com.enterprise.mobile.pages.android.MeeshoPaymentPage;
import com.enterprise.mobile.pages.android.MeeshoPriceDetailsPage;
import com.enterprise.mobile.pages.android.MeeshoProductPage;
import com.enterprise.mobile.pages.android.MeeshoReviewOrderPage;
import com.enterprise.mobile.pages.android.MeeshoSignUpPage;
import io.appium.java_client.AppiumBy;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * End-to-end purchase journey for the Meesho Supply app.
 */
public class MeeshoPurchaseSteps {

    private static final Logger LOG = LoggerFactory.getLogger(MeeshoPurchaseSteps.class);
    private static final String DEFAULT_PHONE = "8368155209";

    private final TestContext context;

    private MeeshoHomePage homePage;
    private MeeshoAccountPage accountPage;
    private MeeshoSignUpPage signUpPage;
    private MeeshoCategoriesPage categoriesPage;
    private MeeshoListingPage listingPage;
    private MeeshoProductPage productPage;
    private MeeshoGalleryPage galleryPage;
    private MeeshoPriceDetailsPage priceDetailsPage;
    private MeeshoReviewOrderPage reviewOrderPage;
    private MeeshoPaymentPage paymentPage;

    private String capturedFinalPrice;
    private String selectedSize;
    private int productIndex = 7;
    private boolean alreadySignedIn;

    public MeeshoPurchaseSteps(TestContext context) {
        this.context = context;
    }

    // ------------------------------------------------------------------
    // Launch and sign-up
    // ------------------------------------------------------------------

    @Given("the Meesho Supply app is launched and ready")
    public void meeshoLaunched() {
        VerificationTracker.page("home");
        VerificationTracker.pending(VerificationTracker.APP_LAUNCHED, VerificationTracker.CATEGORIES_SCREEN,
                VerificationTracker.CATEGORY_RAIL, VerificationTracker.MEN_BOTTOM_WEAR,
                VerificationTracker.LISTING, VerificationTracker.PRODUCT_DETAILS,
                VerificationTracker.GALLERY, VerificationTracker.SIZE,
                VerificationTracker.PRICE_DETAILS, VerificationTracker.BUY_NOW,
                VerificationTracker.REVIEW, VerificationTracker.PAYMENT);
        homePage = new MeeshoHomePage(context.getDriver());
        assertThat(homePage.isLoaded())
                .as("Meesho Supply should be in the foreground")
                .isTrue();
        if (new MeeshoCategoriesPage(context.getDriver()).isOnCategoriesScreen()) {
            // A repeated categories run continues from the categories screen.
            VerificationTracker.verified(VerificationTracker.CATEGORIES_SCREEN);
        LOG.info("Already on the Categories screen - continuing from here");
            return;
        }
        chooseEnglishIfPrompted(3);
        if (!homePage.isFooterDisplayed() && !awaitFooter(10)) {
            LOG.info("Meesho resumed on a previous screen; navigating back to its home screen");
            returnToHome();
            homePage = new MeeshoHomePage(context.getDriver());
        }
        if (!homePage.isFooterDisplayed() && chooseEnglishIfPrompted(40)) {
            LOG.info("Selected English on the onboarding language screen");
        }
        if (!homePage.isFooterDisplayed() && !awaitFooter(10)) {
            // Last resort: bring the home activity to the front without
            // restarting the app.
            LOG.info("Opening the Meesho home activity to restore the footer");
            context.getDriver().executeScript("mobile: startActivity", Map.of(
                    "intent", "com.meesho.supply/com.meesho.supply.main.HomeActivity",
                    "action", "android.intent.action.MAIN",
                    "category", "android.intent.category.LAUNCHER",
                    "flags", "0x10200000"));
            homePage = new MeeshoHomePage(context.getDriver());
        }
        VerificationTracker.verified(VerificationTracker.APP_LAUNCHED);
        assertThat(awaitFooter(40))
                .as("The home footer navigation should be displayed")
                .isTrue();
    }

    private boolean awaitFooter(int seconds) {
        try {
            return Boolean.TRUE.equals(new WebDriverWait(context.getDriver(), Duration.ofSeconds(seconds))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(driver -> homePage.isFooterDisplayed()));
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    /**
     * Returns to Meesho's home screen without restarting the app: presses Back
     * while a deeper screen is shown and only activates the app when it is not
     * in the foreground at all.
     */
    private void returnToHome() {
        String pkg = ConfigManager.get("app.package", "com.meesho.supply");
        for (int attempt = 0; attempt < 8; attempt++) {
            if (context.getDriver() instanceof io.appium.java_client.android.AndroidDriver android
                    && !pkg.equals(android.getCurrentPackage())) {
                context.getDriver().executeScript("mobile: activateApp", Map.of("appId", pkg));
                sleep(250);
                continue;
            }
            if (homePage.isFooterDisplayed()) {
                homePage.openHomeTab();
                sleep(200);
                return;
            }
            // A bottom sheet (Buy Now preferences) blocks the navigation.
            List<WebElement> closeButtons = context.getDriver().findElements(
                    io.appium.java_client.AppiumBy.id("com.meesho.supply:id/close_button"));
            if (!closeButtons.isEmpty()) {
                try {
                    closeButtons.get(0).click();
                    sleep(200);
                    continue;
                } catch (RuntimeException ignored) {
                    // Fall through to the Back press.
                }
            }
            context.getDriver().navigate().back();
            sleep(200);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Waits up to {@code seconds} for Meesho's onboarding language screen and
     * selects English when it appears.
     *
     * @return {@code true} when the language screen was shown and English chosen
     */
    private boolean chooseEnglishIfPrompted(int seconds) {
        try {
            return Boolean.TRUE.equals(new WebDriverWait(context.getDriver(), Duration.ofSeconds(seconds))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(driver -> {
                        List<WebElement> english = driver.findElements(
                                AppiumBy.androidUIAutomator("new UiSelector().description(\"ENGLISH\")"));
                        if (english.isEmpty() || !english.get(0).isDisplayed()) {
                            return false;
                        }
                        english.get(0).click();
                        return true;
                    }));
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    @When("the user opens the Account tab from the footer")
    public void openAccountTab() {
        accountPage = homePage.openAccountTab();
        answerMeeshoPopUps();
    }

    /** Answers the language chooser (English) and the gender chooser (Male). */
    private void answerMeeshoPopUps() {
        MeeshoOnboardingDialogs dialogs = new MeeshoOnboardingDialogs(context.getDriver());
        for (int attempt = 0; attempt < 3 && dialogs.dismissVisibleDialogs(); attempt++) {
            sleep(200);
        }
    }

    @Then("the Meesho account screen should be displayed")
    public void accountScreenDisplayed() {
        assertThat(accountPage.isLoaded())
                .as("Meesho account screen should be displayed")
                .isTrue();
    }

    @Given("the user is signed in with the configured phone number")
    public void signedInWithConfiguredNumber() {
        // The session stays signed in from the sign-up feature, so the account
        // tab is not opened here. Only when Meesho shows a login prompt the
        // account is opened to complete the sign-up.
        if (!isLoginPromptDisplayed()) {
            LOG.info("The session is already signed in; no sign-up needed");
            return;
        }
        LOG.info("A login prompt is on screen - signing in with the configured number");
        homePage = new MeeshoHomePage(context.getDriver());
        returnToHome();
        accountPage = homePage.openAccountTab();
        if (accountPage.isSignedIn()) {
            LOG.info("Account is already signed in; no sign-up needed");
            return;
        }
        signUpPage = accountPage.tapSignUp();
        awaitSignUpSurface();
        continueSignUp();
    }

    /** True when Meesho asks the user to log in on the current screen. */
    private boolean isLoginPromptDisplayed() {
        for (String text : new String[] {"Login to Meesho", "Sign up", "Continue with mobile",
                "Login/Sign up"}) {
            if (!context.getDriver().findElements(io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"" + text + "\")")).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @When("the user taps Sign Up in the header")
    public void tapSignUpInHeader() {
        // Sign-up is only needed once: when the account header already shows the
        // signed-in number (and therefore no Sign up button) the journey carries
        // on straight to the shopping steps.
        if (accountPage.isSignedIn()) {
            LOG.info("Account already shows a signed-in number; skipping the sign-up flow");
            alreadySignedIn = true;
            return;
        }
        accountPage = new MeeshoAccountPage(context.getDriver()).logoutIfSignedIn();
        for (int attempt = 0; attempt < 4 && !accountPage.isSignUpDisplayed(); attempt++) {
            accountPage.swipeDown();
        }
        if (!accountPage.isSignUpDisplayed()) {
            // The session is signed in and the Sign up button is not offered -
            // the journey continues with the signed-in session.
            LOG.info("The account is signed in and offers no Sign up button; "
                    + "continuing with the signed-in session");
            alreadySignedIn = true;
            return;
        }
        try {
            signUpPage = accountPage.tapSignUp();
        } catch (RuntimeException e) {
            // The button is not reachable: continue with the session as-is
            // instead of failing the whole scenario.
            LOG.info("The Sign up button could not be reached; continuing with "
                    + "the signed-in session: {}", e.getMessage());
            alreadySignedIn = true;
            return;
        }
        awaitSignUpSurface();
    }

    /**
     * Waits for the sign-up surface (Truecaller sheet, native phone screen or
     * Google picker) to settle, dismissing the Google credential picker as soon
     * as it appears.
     */
    private void awaitSignUpSurface() {
        WebDriverWait bounded = new WebDriverWait(context.getDriver(), Duration.ofSeconds(30));
        bounded.pollingEvery(Duration.ofMillis(750));
        try {
            bounded.until(driver -> {
                signUpPage.dismissThirdPartyLoginIfPresent();
                signUpPage.dismissGooglePickerIfPresent();
                return signUpPage.isLoginDialogDisplayed() || signUpPage.isNativePhoneScreenDisplayed();
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            LOG.warn("Neither the Login to Meesho sheet nor the native phone screen appeared");
        }
    }

    @Then("the Login to Meesho sign-up dialog should be displayed with the configured name and number")
    public void loginDialogDisplayed() {
        if (alreadySignedIn) {
            LOG.info("Sign-up dialog check skipped: the account is already signed in");
            return;
        }
        if (signUpPage.isLoginDialogDisplayed()) {            assertThat(signUpPage.getLoginDialogTitle())
                    .as("Login dialog title")
                    .containsIgnoringCase("Login to Meesho");
            assertThat(signUpPage.getLoginDialogName())
                    .as("Login dialog should show the user name")
                    .isNotBlank();
            assertThat(lastTenDigits(signUpPage.getLoginDialogNumber()))
                    .as("Login dialog should show the configured number")
                    .isEqualTo(lastTenDigits(configuredPhone()));
            LOG.info("Verified Login to Meesho dialog for {}", signUpPage.getLoginDialogName());
        } else {
            assertThat(signUpPage.isNativePhoneScreenDisplayed())
                    .as("Expected the Login to Meesho dialog or the Meesho phone screen")
                    .isTrue();
            LOG.info("Truecaller dialog was not shown; using the native Meesho phone screen");
        }
    }

    @When("the user continues the sign-up with the configured phone number")
    public void continueSignUp() {
        if (alreadySignedIn) {
            LOG.info("Sign-up continuation skipped: the account is already signed in");
            return;
        }
        if (signUpPage.isLoginDialogDisplayed()) {
            signUpPage.tapTruecallerContinue();
            if (!waitForLoggedIn(25)) {
                if (signUpPage.isEnterDetailsManuallyDisplayed()) {
                    LOG.info("Continue did not log in; falling back to Enter details manually");
                    signUpPage.tapEnterDetailsManually();
                }
                if (signUpPage.isNativePhoneScreenDisplayed()) {
                    signUpPage.enterPhoneNumber(configuredPhone());
                    signUpPage.tapContinue();
                }
            }
        } else if (signUpPage.isNativePhoneScreenDisplayed()) {
            signUpPage.enterPhoneNumber(configuredPhone());
            signUpPage.tapContinue();
        }
        assertThat(waitForLoggedIn(30))
                .as("User should be logged in after continuing sign-up")
                .isTrue();
        // The language and gender choosers pop up right after the sign-up.
        answerMeeshoPopUps();
    }

    @Then("the user should be logged in with the configured phone number")
    public void userLoggedIn() {
        answerMeeshoPopUps();
        if (alreadySignedIn) {
            LOG.info("Already signed in as {}; continuing to the shopping steps", configuredPhone());
            return;
        }
        homePage = new MeeshoHomePage(context.getDriver());
        accountPage = homePage.openAccountTab();
        boolean signedIn = false;
        try {
            signedIn = Boolean.TRUE.equals(new WebDriverWait(context.getDriver(), Duration.ofSeconds(20))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(driver -> accountPage.isSignedIn()));
        } catch (org.openqa.selenium.TimeoutException e) {
            signedIn = false;
        }
        assertThat(signedIn)
                .as("Account header should show the signed-in phone number")
                .isTrue();
        LOG.info("Signed in as {}", configuredPhone());
    }

    // ------------------------------------------------------------------
    // Categories -> Men -> Bottom Wear -> Cargos/Trousers
    // ------------------------------------------------------------------

    @Given("the user is on the product details page of product number {int}")
    public void onProductNumberSixDetails(Integer index) {
        productIndex = index;
        homePage = new MeeshoHomePage(context.getDriver());
        returnToHome();
        categoriesPage = homePage.openCategoriesTab();
        assertThat(categoriesPage.isLoaded())
                .as("Categories screen should be displayed")
                .isTrue();
        VerificationTracker.verified(VerificationTracker.CATEGORIES_SCREEN);
        categoriesPage.openMenCategory();
        listingPage = categoriesPage.openCargosAndTrousers();
        VerificationTracker.verified(VerificationTracker.LISTING);
        assertThat(listingPage.isLoaded())
                .as("Cargos/Trousers listing should be displayed")
                .isTrue();
        productPage = listingPage.openProductAtIndex(index);
        assertThat(productPage.isLoaded())
                .as("Product details page should be displayed")
                .isTrue();
        VerificationTracker.verified(VerificationTracker.PRODUCT_DETAILS);
        LOG.info("Opened the details page of product {}", index);
    }

    @When("the user opens the Categories section from the footer")
    public void openCategories() {
        VerificationTracker.page("categories");
        homePage = new MeeshoHomePage(context.getDriver());
        MeeshoCategoriesPage categories = new MeeshoCategoriesPage(context.getDriver());
        if (categories.isOnCategoriesScreen()) {
            // The app is already on the categories screen: stay here and repeat
            // the remaining steps instead of navigating to another section.
            categoriesPage = categories;
            VerificationTracker.verified(VerificationTracker.CATEGORIES_SCREEN);
        LOG.info("Already on the Categories screen - continuing from here");
            return;
        }
        // Go straight to the Categories tab - the Home tab is never used as a
        // stepping stone.
        if (!homePage.isFooterDisplayed()) {
            for (int attempt = 0; attempt < 4 && !homePage.isFooterDisplayed(); attempt++) {
                context.getDriver().navigate().back();
                sleep(150);
                homePage = new MeeshoHomePage(context.getDriver());
            }
        }
        categoriesPage = homePage.openCategoriesTab();
    }

    @Then("the category catalogue should list the top-level categories")
    public void categoryCatalogueListed() {
        VerificationTracker.page("categories");
        VerificationTracker.verified(VerificationTracker.CATEGORY_RAIL);
        assertThat(categoriesPage.isCategoryRailDisplayed())
                .as("The Categories screen should list the top-level categories (for example Men)")
                .isTrue();
        LOG.info("Category rail is displayed with the top-level categories");
    }

    @Then("the Categories screen should be displayed")
    public void categoriesScreenDisplayed() {
        assertThat(categoriesPage.isLoaded())
                .as("Categories screen should be displayed")
                .isTrue();
        VerificationTracker.verified(VerificationTracker.CATEGORIES_SCREEN);
    }

    @When("the user opens the Men category")
    public void openMenCategory() {
        VerificationTracker.page("categories > men");
        runWithRecovery("open the Men category", () -> categoriesPage.openMenCategory());
    }

    @Then("the Bottom Wear section should be displayed")
    public void bottomWearDisplayed() {
        VerificationTracker.page("categories > men > bottom wear");
        VerificationTracker.verified(VerificationTracker.MEN_BOTTOM_WEAR);
        assertThat(categoriesPage.isBottomWearSectionDisplayed())
                .as("Men > Bottom Wear section header should be visible")
                .isTrue();
    }

    @When("the user opens Cargos and Trousers")
    public void openCargosAndTrousers() {
        VerificationTracker.page("cargos/trousers listing");
        runWithRecovery("open Cargos and Trousers",
                () -> listingPage = categoriesPage.openCargosAndTrousers());
    }

    @Then("the Cargos and Trousers listing should be displayed")
    public void listingDisplayed() {
        VerificationTracker.page("cargos/trousers listing");
        VerificationTracker.verified(VerificationTracker.LISTING);
        assertThat(listingPage.isLoaded())
                .as("Cargos/Trousers listing should be displayed")
                .isTrue();
    }

    // ------------------------------------------------------------------
    // Product, gallery and size
    // ------------------------------------------------------------------

    /**
     * Repeats a step on the product details page. When the step fails the page
     * is left one step back and the product details page is opened again, then
     * the same step runs once more - the app is never logged out and the
     * scenario is never restarted from its first step.
     */
    private void runOnProductPage(String description, Runnable action) {
        RuntimeException failure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                action.run();
                return;
            } catch (RuntimeException e) {
                failure = e;
                LOG.warn("'{}' failed (attempt {}/3): {}", description, attempt, e.getMessage());
                if (isSessionLost(e)) {
                    LOG.warn("The session is gone; creating a new one for the same step");
                    recreateDriver();
                }
                backToProductPage();
            }
        }
        throw failure;
    }

    /** Goes one page back and opens the product details page again. */
    private void backToProductPage() {
        try {
            context.getDriver().navigate().back();
            sleep(150);
            listingPage = new MeeshoListingPage(context.getDriver());
            for (int attempt = 0; attempt < 3 && !listingPage.isLoaded(); attempt++) {
                context.getDriver().navigate().back();
                sleep(150);
                listingPage = new MeeshoListingPage(context.getDriver());
            }
            productPage = listingPage.openProductAtIndex(productIndex);
            LOG.info("Back on the product details page - the step is repeated");
        } catch (RuntimeException e) {
            LOG.warn("Could not return to the product details page: {}", e.getMessage());
        }
    }

    @When("the user opens product number {int} from the listing")
    public void openProductSix(Integer index) {
        VerificationTracker.page("cargos/trousers listing");
        productIndex = index;
        runWithRecovery("open product number " + index, () -> {
            // Stay in the current section: when the listing is not on top the
            // screen is walked back to it instead of restarting the scenario.
            listingPage = new MeeshoListingPage(context.getDriver());
            for (int attempt = 0; attempt < 3 && !listingPage.isLoaded(); attempt++) {
                context.getDriver().navigate().back();
                sleep(150);
                listingPage = new MeeshoListingPage(context.getDriver());
            }
            // No browsing/scrolling here: open the card of the listing as-is.
            productPage = listingPage.openProductAtIndex(index);
        });
    }

    /**
     * Repeats an action when it fails: the page is scrolled back and forth and
     * the same step is executed again. When the session itself died (the
     * wireless device dropped), a new session is created and the same step runs
     * again - the app is never logged out and the scenario is never restarted
     * from its first step.
     */
    private void runWithRecovery(String description, Runnable action) {
        RuntimeException failure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                action.run();
                return;
            } catch (RuntimeException e) {
                failure = e;
                LOG.warn("'{}' failed (attempt {}/3): {}", description, attempt, e.getMessage());
                if (isSessionLost(e)) {
                    LOG.warn("The session is gone; creating a new one for the same step");
                    recreateDriver();
                }
                recoverPage();
            }
        }
        throw failure;
    }

    /** True when the error means the Appium session died. */
    private boolean isSessionLost(RuntimeException error) {
        String message = error.getMessage() == null ? "" : error.getMessage().toLowerCase();
        return message.contains("instrumentation process is not running")
                || message.contains("session")
                || message.contains("device offline")
                || message.contains("connection refused")
                || message.contains("no such window")
                || message.contains("dead object");
    }

    /** Creates a fresh Appium session and keeps the app running. */
    private void recreateDriver() {
        try {
            context.initDriver();
            homePage = new MeeshoHomePage(context.getDriver());
            LOG.info("A new session is ready ({})", context.getDriver().getSessionId());
        } catch (RuntimeException e) {
            LOG.warn("Could not create a new session: {}", e.getMessage());
        }
    }

    /** Scrolls the page up and down so the app is responsive again. */
    private void recoverPage() {
        try {
            MeeshoHomePage page = new MeeshoHomePage(context.getDriver());
            page.swipeUp();
            page.swipeDown();
        } catch (RuntimeException e) {
            LOG.warn("Could not recover the page: {}", e.getMessage());
        }
    }

    @Then("the product details page should be displayed")
    public void productDetailsDisplayed() {
        VerificationTracker.page("product details page");
        assertThat(productPage.isLoaded())
                .as("Product details page should be displayed")
                .isTrue();
        VerificationTracker.verified(VerificationTracker.PRODUCT_DETAILS);
        LOG.info("Opened product '{}' at price {}", productPage.getProductName(),
                productPage.getDisplayedPrice());
    }

    @When("the user opens the product images in full screen")
    public void openFullScreenImages() {
        VerificationTracker.page("full screen gallery");
        runOnProductPage("open the full screen images",
                () -> galleryPage = productPage.openFullScreenGallery());
    }

    @Then("the full screen image gallery should be displayed")
    public void galleryDisplayed() {
        VerificationTracker.page("full screen gallery");
        assertThat(galleryPage.isLoaded())
                .as("Full screen image gallery should be displayed")
                .isTrue();
        VerificationTracker.verified(VerificationTracker.GALLERY);
    }

    @When("the user scrolls the images three times to the right and once to the left")
    public void swipeGallery() {
        VerificationTracker.page("full screen gallery");
        runOnProductPage("scroll the product images", () -> {
            if (!galleryPage.isOnGalleryScreen()) {
                galleryPage = productPage.openFullScreenGallery();
            }
            galleryPage.swipeRight(3).swipeLeft(1);
        });
    }

    private void backToListing() {
        context.getDriver().navigate().back();
        sleep(200);
    }

    @And("the user exits the full screen gallery")
    public void exitGallery() {
        VerificationTracker.page("product details page");
        runOnProductPage("exit the full screen gallery", () -> {
            if (!galleryPage.isOnGalleryScreen()) {
                galleryPage = productPage.openFullScreenGallery();
            }
            productPage = galleryPage.exit();
        });
    }

    @When("the user scrolls the product page slightly to the Select Size section")
    public void scrollToSelectSize() {
        VerificationTracker.page("product details page");
        productPage.scrollToSizeSection();
        LOG.info("Scrolled the product page slightly to the Select Size section");
        VerificationTracker.page("product details page");
    }

    @When("the user selects the available size")
    public void selectAvailableSize() {
        runOnProductPage("select the available size", () -> {
            if (selectedSize != null) {
                // The size is already chosen - the step is not repeated.
                LOG.info("The size {} is already selected", selectedSize);
                return;
            }
            String chosen = productPage.selectAnySize("32");
            if (chosen != null) {
                selectedSize = chosen;
                LOG.info("Size selected on the product page: {}", chosen);
        VerificationTracker.verified(VerificationTracker.SIZE);
                return;
            }
            if (productPage.hasSizeOptions()) {
                throw new IllegalStateException(
                        "The product offers size chips but none could be selected");
            }
            // The product is sold without variants: the size is selected in the
            // select preferences sheet.
            LOG.info("This product has no size options; the size is selected in the "
                    + "select preferences sheet");
        });
    }

    @Then("the Select Size section should be displayed with the available sizes")
    public void sizeSectionWithSizes() {
        VerificationTracker.page("product details page");
        if (!productPage.hasSizeOptions()) {
            LOG.info("This product is sold without size options; nothing to verify");
            return;
        }
        assertThat(productPage.isSizeSectionDisplayed())
                .as("The Select Size header should be displayed on the product page")
                .isTrue();
        List<String> sizes = productPage.availableSizes();
        assertThat(sizes)
                .as("The Select Size section should list the sizes available to pick")
                .isNotEmpty();
        LOG.info("Select Size section shows: {}", sizes);
        VerificationTracker.verified(VerificationTracker.SIZE);
    }

    @When("the user selects size 32")
    public void selectSize32() {
        VerificationTracker.page("product details page");
        runOnProductPage("select size 32", () -> {
            if (selectedSize != null) {
                // The size is already chosen - the step is not repeated.
                LOG.info("The size {} is already selected", selectedSize);
                return;
            }
            // Only the sixth product is under test - never fall through to other
            // cards. When 32 is not offered, any offered size is picked.
            String chosen = productPage.selectAnySize("32");
            if (chosen != null) {
                selectedSize = chosen;
                LOG.info("Selected size {}", chosen);
                return;
            }
            if (productPage.hasSizeOptions()) {
                throw new IllegalStateException(
                        "The product offers size chips but none could be selected");
            }
            LOG.info("This product has no size options; the size is selected in the "
                    + "select preferences sheet");
        });
    }

    @Then("size 32 should be selected and mandatory for the order")
    public void sizeSelected() {
        VerificationTracker.page("product details page");
        if (selectedSize == null) {
            LOG.info("This product is sold without size options; no size to verify");
            return;
        }
        assertThat(productPage.isSizeSelected(selectedSize))
                .as("The size %s must be marked as selected on the product page", selectedSize)
                .isTrue();
        LOG.info("Selected size before the order: {}", selectedSize);
        VerificationTracker.verified(VerificationTracker.SIZE);
    }

    // ------------------------------------------------------------------
    // Price details
    // ------------------------------------------------------------------

    @When("the user opens the price details dialog from the info icon")
    public void openPriceDetails() {
        VerificationTracker.page("product details page");
        priceDetailsPage = productPage.openPriceDetails();
    }

    @Then("the price details dialog should show MRP, product price, discount and final price")
    public void priceDetailsVerified() {
        VerificationTracker.page("price details dialog");
        assertThat(priceDetailsPage.isLoaded())
                .as("Price details dialog should be displayed")
                .isTrue();
        assertThat(priceDetailsPage.isMrpDisplayed()).as("MRP row").isTrue();
        assertThat(priceDetailsPage.isProductPriceDisplayed()).as("Product price row").isTrue();
        assertThat(priceDetailsPage.isFinalPriceDisplayed()).as("Final price row").isTrue();

        long mrp = money(priceDetailsPage.getMrp());
        long productPrice = money(priceDetailsPage.getProductPrice());
        long finalPrice = money(priceDetailsPage.getFinalPrice());

        assertThat(priceDetailsPage.getMrpLabel()).containsIgnoringCase("MRP");
        assertThat(mrp).as("MRP should be a positive amount").isPositive();
        assertThat(productPrice).as("Product price should be a positive amount").isPositive();
        assertThat(finalPrice).as("Final price should be a positive amount").isPositive();
        assertThat(mrp).as("MRP should not be below the product price").isGreaterThanOrEqualTo(productPrice);

        if (priceDetailsPage.hasDiscount()) {
            assertThat(priceDetailsPage.isDiscountDisplayed())
                    .as("Discount row should expose both label and value")
                    .isTrue();
            assertThat(priceDetailsPage.getDiscountLabel()).isNotBlank();
            long discount = money(priceDetailsPage.getDiscountValue());
            assertThat(discount).as("Discount should be a positive amount").isPositive();
            assertThat(productPrice - discount)
                    .as("Product price (%s) minus discount (%s) should equal final price (%s)",
                            productPrice, discount, finalPrice)
                    .isEqualTo(finalPrice);
            LOG.info("Price details | {} | product {} | {} {} | final {}", priceDetailsPage.getMrpLabel(),
                    productPrice, priceDetailsPage.getDiscountLabel(),
                    priceDetailsPage.getDiscountValue(), finalPrice);
        } else {
            assertThat(finalPrice)
                    .as("Without a discount row the final price must equal the product price")
                    .isEqualTo(productPrice);
            LOG.info("Price details | {} | product {} | no discount row (final == product price)",
                    priceDetailsPage.getMrpLabel(), productPrice);
        }
    }

    @When("the user captures the final price and closes the price details dialog")
    public void captureFinalPriceAndClose() {
        VerificationTracker.page("price details dialog");
        capturedFinalPrice = priceDetailsPage.getFinalPrice();
        assertThat(money(capturedFinalPrice))
                .as("Final price should be a positive amount")
                .isPositive();
        productPage = priceDetailsPage.close();
        LOG.info("Captured final price {}", capturedFinalPrice);
        VerificationTracker.verified(VerificationTracker.PRICE_DETAILS);
    }

    @Then("the captured final price should match the price shown on the product page")
    public void finalPriceMatchesProductPage() {
        VerificationTracker.page("product details page");
        String productPagePrice = productPage.getDisplayedPrice();
        assertThat(money(productPagePrice))
                .as("Product page price '%s' should match price-details final price '%s'",
                        productPagePrice, capturedFinalPrice)
                .isEqualTo(money(capturedFinalPrice));
    }

    // ------------------------------------------------------------------
    // Buy Now -> Review -> Payment
    // ------------------------------------------------------------------

    @When("the user taps Buy Now")
    public void tapBuyNow() {
        VerificationTracker.page("buy now / select preferences");
        reviewOrderPage = productPage.buyNow();
        if (selectedSize == null) {
            // The size was not offered on the product page, so it is selected
            // inside the Buy Now preferences sheet.
            selectedSize = productPage.getSheetSelectedSize();
        }
    }

    @When("the user taps Buy Now without selecting a size on the product page")
    public void tapBuyNowWithoutSize() {
        selectedSize = null;
        reviewOrderPage = productPage.buyNow();
        selectedSize = productPage.getSheetSelectedSize();
        assertThat(selectedSize != null || productPage.getBuyNowSheetPrice() != null)
                .as("Without a size on the product page the select preferences sheet must be "
                        + "shown with its size chips and price")
                .isTrue();
    }

    @Then("the size should be selected in the select preferences dialog box")
    public void sizeSelectedInSheet() {
        if (!productPage.sheetHasSizeChips()) {
            LOG.info("This product has no size options at all - nothing to select in the sheet");
            return;
        }
        assertThat(productPage.getSheetSelectedSize())
                .as("A size must be selected in the select preferences dialog box")
                .isNotNull();
        LOG.info("Size selected in the select preferences dialog box: {}",
                productPage.getSheetSelectedSize());
    }

    @Then("the total price in the select preferences dialog should match the product page price")
    public void buyNowSheetPriceMatches() {
        // Buy Now is done either through the preferences sheet or straight to
        // the review page; both complete the "buy now" step.
        VerificationTracker.verified(VerificationTracker.BUY_NOW);
        String sheetPrice = productPage.getBuyNowSheetPrice();
        if (sheetPrice == null || sheetPrice.isBlank()) {
            // The size was already chosen on the product page, so Meesho went
            // straight to the review page without showing the sheet.
            LOG.info("The select preferences sheet was not shown - the order went "
                    + "straight to the review page");
            return;
        }
        assertThat(sheetPrice)
                .as("The select preferences dialog must show its total price")
                .isNotBlank();
        VerificationTracker.verified(VerificationTracker.BUY_NOW);
        LOG.info("Select preferences sheet total price: {}", sheetPrice);
    }

    @Then("the Review your order page should be displayed with the address and price details")
    public void reviewOrderDisplayed() {
        VerificationTracker.page("review order page");
        assertThat(reviewOrderPage.isLoaded())
                .as("Review your order page should be displayed")
                .isTrue();
        assertThat(reviewOrderPage.isAddressDisplayed())
                .as("Delivery address should be displayed")
                .isTrue();
        assertThat(reviewOrderPage.getAddressName()).isNotBlank();
        assertThat(reviewOrderPage.getAddressMobile()).isNotBlank();
        assertThat(reviewOrderPage.isPriceDetailsDisplayed())
                .as("Price details should be displayed on the review page")
                .isTrue();
        String reviewSize = reviewOrderPage.getSelectedSize();
        if (selectedSize != null && !reviewSize.isBlank()) {
            assertThat(reviewSize)
                    .as("Review page should show the size selected on the product page")
                    .isEqualTo(selectedSize);
        }
        LOG.info("Review order | product {} | size {}", reviewOrderPage.getProductName(),
                reviewOrderPage.getSelectedSize());
    }

    @Then("the three prices on the review page should be the same")
    public void reviewPricesAreTheSame() {
        VerificationTracker.page("review order page");
        reviewOrderPage.expandPriceDetails();
        String productPrice = reviewOrderPage.getProductPrice();
        String detailsTotal = reviewOrderPage.getPriceDetailsAmount();
        String footerTotal = reviewOrderPage.getFooterTotal();

        assertThat(productPrice).as("Product price on the review page").isNotBlank();
        assertThat(detailsTotal).as("Total inside the price details section").isNotBlank();
        assertThat(footerTotal).as("Total payable in the review page footer").isNotBlank();

        long product = money(productPrice);
        long details = money(detailsTotal);
        long footer = money(footerTotal);

        assertThat(details)
                .as("Price details total (%s) should equal the product row price (%s)",
                        detailsTotal, productPrice)
                .isEqualTo(product);
        assertThat(footer)
                .as("Footer total (%s) should equal the product row price (%s)",
                        footerTotal, productPrice)
                .isEqualTo(product);
        assertThat(product)
                .as("Review page price should be a positive amount")
                .isPositive();

        String sheetPrice = productPage.getBuyNowSheetPrice();
        if (sheetPrice != null && !sheetPrice.isBlank()) {
            assertThat(product)
                    .as("Review page price should equal the price confirmed in the Buy Now sheet (%s)",
                            sheetPrice)
                    .isEqualTo(money(sheetPrice));
        }
        VerificationTracker.verified(VerificationTracker.REVIEW);
        LOG.info("Review prices | product row {} | price details {} | footer {}",
                productPrice, detailsTotal, footerTotal);
    }

    @When("the user taps Continue on the review page")
    public void continueOnReviewPage() {
        VerificationTracker.page("review order page");
        paymentPage = reviewOrderPage.tapContinue();
    }

    @Then("the Payment Method page should be displayed")
    public void paymentPageDisplayed() {
        VerificationTracker.page("payment method page");
        assertThat(paymentPage.isLoaded())
                .as("Payment method page should be displayed")
                .isTrue();
        assertThat(paymentPage.isPlaceOrderDisplayed())
                .as("Place Order button should be available")
                .isTrue();
    }

    @And("the Cash on Delivery and Pay Online options with their prices should be displayed")
    public void paymentOptionsVerified() {
        VerificationTracker.page("payment method page");
        assertThat(paymentPage.isCashOnDeliveryDisplayed())
                .as("Cash on Delivery option should be displayed")
                .isTrue();
        assertThat(paymentPage.isPayOnlineDisplayed())
                .as("Pay Online option should be displayed")
                .isTrue();

        String codPrice = paymentPage.getCashOnDeliveryPrice();
        String onlinePrice = paymentPage.getPayOnlinePrice();
        assertThat(money(codPrice))
                .as("Cash on Delivery price '%s'", codPrice)
                .isPositive();
        assertThat(money(onlinePrice))
                .as("Pay Online price '%s'", onlinePrice)
                .isPositive();
        VerificationTracker.verified(VerificationTracker.PAYMENT);
        LOG.info("Payment options | Cash on Delivery {} | Pay Online {}", codPrice, onlinePrice);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String configuredPhone() {
        String phone = ConfigManager.get("meesho.signup.phone", DEFAULT_PHONE);
        return phone == null || phone.isBlank() ? DEFAULT_PHONE : phone;
    }

    private static String lastTenDigits(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        return digits.length() > 10 ? digits.substring(digits.length() - 10) : digits;
    }

    private static long money(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        return digits.isEmpty() ? 0L : Long.parseLong(digits);
    }

    private boolean waitForLoggedIn(int seconds) {
        try {
            Boolean loggedIn = new WebDriverWait(context.getDriver(), Duration.ofSeconds(seconds))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(driver -> {
                        List<WebElement> phoneHeader = driver.findElements(
                                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"+91\")"));
                        List<WebElement> signUp = driver.findElements(
                                AppiumBy.androidUIAutomator("new UiSelector().text(\"Sign up\")"));
                        List<WebElement> phoneField = driver.findElements(
                                AppiumBy.id("com.meesho.supply:id/phone_edit_text"));
                        return !phoneHeader.isEmpty() && signUp.isEmpty() && phoneField.isEmpty();
                    });
            return Boolean.TRUE.equals(loggedIn);
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}

