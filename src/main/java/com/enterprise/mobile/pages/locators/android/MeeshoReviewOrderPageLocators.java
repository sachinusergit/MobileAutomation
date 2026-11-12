package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoReviewOrderPage} ("REVIEW YOUR ORDER", STEP 1/2,
 * MultiCartActivity). Plain data class — no behaviour.
 */
public class MeeshoReviewOrderPageLocators {

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"REVIEW YOUR ORDER\")")
    public WebElement title;

    @AndroidFindBy(uiAutomator = "new UiSelector().textContains(\"STEP 1/2\")")
    public WebElement step;

    @AndroidFindBy(id = "com.meesho.supply:id/product_name")
    public WebElement productName;

    @AndroidFindBy(id = "com.meesho.supply:id/offerPrice")
    public WebElement offerPrice;

    @AndroidFindBy(id = "com.meesho.supply:id/orignal_striked_off_price")
    public WebElement originalPrice;

    @AndroidFindBy(id = "com.meesho.supply:id/sub_title")
    public WebElement discountSubTitle;

    @AndroidFindBy(id = "com.meesho.supply:id/variation_name")
    public WebElement variationName;

    @AndroidFindBy(id = "com.meesho.supply:id/quantity_value")
    public WebElement quantityValue;

    @AndroidFindBy(id = "com.meesho.supply:id/supplier_info")
    public WebElement supplierInfo;

    @AndroidFindBy(id = "com.meesho.supply:id/delivery_address_title")
    public WebElement deliveryTitle;

    @AndroidFindBy(id = "com.meesho.supply:id/name_text_view")
    public WebElement addressName;

    @AndroidFindBy(id = "com.meesho.supply:id/mobile_number")
    public WebElement addressMobile;

    @AndroidFindBy(id = "com.meesho.supply:id/change_cta")
    public WebElement changeAddress;

    @AndroidFindBy(id = "com.meesho.supply:id/price_details_title")
    public WebElement priceDetailsTitle;

    @AndroidFindBy(id = "com.meesho.supply:id/amount_title")
    public WebElement priceDetailsAmount;

    @AndroidFindBy(id = "com.meesho.supply:id/multi_cta_info_primary_cta")
    public WebElement continueButton;

    @AndroidFindBy(id = "com.meesho.supply:id/multi_cta_info_secondary_cta")
    public WebElement viewPriceDetails;

    @AndroidFindBy(id = "com.meesho.supply:id/multi_cta_info_overlapping_text")
    public WebElement footerAmount;

    @AndroidFindBy(id = "com.meesho.supply:id/sub_info_text")
    public WebElement footerDiscount;
}
