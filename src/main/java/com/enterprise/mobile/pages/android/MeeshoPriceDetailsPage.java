package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoPriceDetailsPageLocators;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * "PRICE DETAILS" bottom sheet opened from the product page info icon. It lists
 * the MRP, the supplier product price, the applicable discount and the final
 * price for the selected variant.
 */
public class MeeshoPriceDetailsPage extends BasePage {

    private final MeeshoPriceDetailsPageLocators locators;

    public MeeshoPriceDetailsPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoPriceDetailsPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        boolean loaded = awaitAnyDisplayed(20, locators.finalPriceValue);
        dumpPageSource("price-details-dialog");
        return loaded;
    }

    /** Writes the dialog to target/screenshots/debug for analysis. */
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

    public boolean isMrpDisplayed() {
        return isDisplayed(locators.mrpLabel) && isDisplayed(locators.mrpValue);
    }

    public boolean isProductPriceDisplayed() {
        return isDisplayed(locators.productPriceLabel) && isDisplayed(locators.productPriceValue);
    }

    public boolean isDiscountDisplayed() {
        return isDisplayed(locators.discountLabel) && isDisplayed(locators.discountValue);
    }

    /** @return {@code true} when the sheet includes a discount row at all. */
    public boolean hasDiscount() {
        return isDisplayed(locators.discountLabel) || isDisplayed(locators.discountValue);
    }

    public boolean isFinalPriceDisplayed() {
        return isDisplayed(locators.finalPriceLabel) && isDisplayed(locators.finalPriceValue);
    }

    public String getMrpLabel() {
        return textOf(locators.mrpLabel);
    }

    public String getMrp() {
        return textOf(locators.mrpValue);
    }

    public String getProductPrice() {
        return textOf(locators.productPriceValue);
    }

    public String getDiscountLabel() {
        return textOf(locators.discountLabel);
    }

    public String getDiscountValue() {
        return textOf(locators.discountValue);
    }

    public String getFinalPrice() {
        return textOf(locators.finalPriceValue);
    }

    /** Closes the sheet and returns to the product page. */
    public MeeshoProductPage close() {
        tap(locators.okButton);
        return new MeeshoProductPage(driver);
    }
}
