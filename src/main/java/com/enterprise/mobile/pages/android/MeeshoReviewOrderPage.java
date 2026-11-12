package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoReviewOrderPageLocators;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * "REVIEW YOUR ORDER" page (STEP 1/2, MultiCartActivity). Confirms the
 * delivery address, the selected size, and the order price details before
 * continuing to payment.
 */
public class MeeshoReviewOrderPage extends BasePage {

    private final MeeshoReviewOrderPageLocators locators;

    public MeeshoReviewOrderPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoReviewOrderPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        boolean loaded = awaitAnyDisplayed(30, locators.step, locators.priceDetailsTitle, locators.continueButton);
        if (loaded) {
            dumpPageSource("review-order");
        }
        return loaded;
    }

    public String getProductName() {
        return textOf(locators.productName);
    }

    public String getOfferPrice() {
        return textOf(locators.offerPrice);
    }

    /**
     * @return just the size value, e.g. {@code "32"} from the row text
     * {@code "Size: 32"}.
     */
    public String getSelectedSize() {
        return textOf(locators.variationName).replaceAll("(?i)^\\s*size\\s*:?\\s*", "").trim();
    }

    public String getQuantity() {
        return textOf(locators.quantityValue);
    }

    public String getAddressName() {
        return textOf(locators.addressName);
    }

    public String getAddressMobile() {
        return textOf(locators.addressMobile);
    }

    public String getDeliveryText() {
        return textOf(locators.deliveryTitle);
    }

    public String getPriceDetailsAmount() {
        return textOf(locators.priceDetailsAmount);
    }

    /** Price shown on the product row of the review page. */
    public String getProductPrice() {
        return textOf(locators.offerPrice);
    }

    /** Total payable shown in the sticky footer of the review page. */
    public String getFooterTotal() {
        return textOf(locators.footerAmount);
    }

    /**
     * The price breakdown of the review page is collapsed by default; opening
     * it reveals the total amount inside the price details section.
     */
    public MeeshoReviewOrderPage expandPriceDetails() {
        if (!isDisplayed(locators.priceDetailsAmount)) {
            tap(locators.viewPriceDetails);
        }
        return this;
    }

    public boolean isAddressDisplayed() {
        return isVisibleAfterScrolling(
                io.appium.java_client.AppiumBy.id("com.meesho.supply:id/name_text_view"))
                && isVisibleAfterScrolling(
                io.appium.java_client.AppiumBy.id("com.meesho.supply:id/mobile_number"))
                && isVisibleAfterScrolling(
                io.appium.java_client.AppiumBy.id("com.meesho.supply:id/delivery_address_title"));
    }

    public boolean isPriceDetailsDisplayed() {
        return isVisibleAfterScrolling(
                io.appium.java_client.AppiumBy.id("com.meesho.supply:id/price_details_title"));
    }

    public MeeshoPaymentPage tapContinue() {
        clickByScrolling(io.appium.java_client.AppiumBy.id(
                "com.meesho.supply:id/multi_cta_info_primary_cta"));
        return new MeeshoPaymentPage(driver);
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
