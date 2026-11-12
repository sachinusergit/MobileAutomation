package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoPaymentPageLocators;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * "PAYMENT METHOD" page (STEP 2/2, CheckoutJuspayPaymentActivity). The two
 * headline options, Cash on Delivery and Pay Online, share the same price id,
 * so each option's price is read relative to its title.
 */
public class MeeshoPaymentPage extends BasePage {

    private final MeeshoPaymentPageLocators locators;

    public MeeshoPaymentPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoPaymentPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return awaitAnyDisplayed(30, locators.selectPaymentMethod, locators.cashOnDelivery,
                locators.payOnline);
    }

    public boolean isCashOnDeliveryDisplayed() {
        return isVisibleAfterScrolling(AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"Cash on Delivery\")"));
    }

    public boolean isPayOnlineDisplayed() {
        return isVisibleAfterScrolling(AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"Pay Online\")"));
    }

    public String getCashOnDeliveryPrice() {
        return priceFor("Cash on Delivery");
    }

    public String getPayOnlinePrice() {
        return priceFor("Pay Online");
    }

    public boolean isPlaceOrderDisplayed() {
        return isDisplayed(locators.placeOrder);
    }

    /**
     * Reads the price that belongs to the given payment option. Both options
     * share the same price id, so the price is the one rendered on the same
     * row as the option title.
     */
    private String priceFor(String title) {
        WebElement optionTitle = driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"" + title + "\")"));
        Rectangle titleBox = optionTitle.getRect();
        for (WebElement price : driver.findElements(
                AppiumBy.id("com.meesho.supply:id/price_tv"))) {
            try {
                Rectangle priceBox = price.getRect();
                if (Math.abs(priceBox.getY() - titleBox.getY()) < 80) {
                    return price.getText().trim();
                }
            } catch (RuntimeException ignored) {
                // Price re-rendered; check the next one.
            }
        }
        throw new IllegalStateException(
                "No price is shown next to the '" + title + "' option");
    }
}
