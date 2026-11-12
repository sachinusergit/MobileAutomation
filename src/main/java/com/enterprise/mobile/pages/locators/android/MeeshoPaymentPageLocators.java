package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoPaymentPage} ("PAYMENT METHOD", STEP 2/2,
 * CheckoutJuspayPaymentActivity). Plain data class — payment option rows are
 * matched by their title text in the page object because both rows share the
 * same price id.
 */
public class MeeshoPaymentPageLocators {

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"PAYMENT METHOD\")")
    public WebElement title;

    @AndroidFindBy(uiAutomator = "new UiSelector().textContains(\"STEP 2/2\")")
    public WebElement step;

    @AndroidFindBy(id = "com.meesho.supply:id/tvSelectPaymentMethod")
    public WebElement selectPaymentMethod;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Cash on Delivery\")")
    public WebElement cashOnDelivery;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Pay Online\")")
    public WebElement payOnline;

    @AndroidFindBy(id = "com.meesho.supply:id/tv_savings_text")
    public WebElement savingsText;

    @AndroidFindBy(id = "com.meesho.supply:id/price_details_title")
    public WebElement priceDetailsTitle;

    @AndroidFindBy(id = "com.meesho.supply:id/multi_cta_info_primary_cta")
    public WebElement placeOrder;

    @AndroidFindBy(id = "com.meesho.supply:id/multi_cta_info_secondary_cta")
    public WebElement viewPriceDetails;

    @AndroidFindBy(id = "com.meesho.supply:id/multi_cta_info_text")
    public WebElement footerAmount;
}
