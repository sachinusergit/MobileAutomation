package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoPriceDetailsPage} (the "PRICE DETAILS" bottom
 * sheet opened from the product page info icon). Plain data class — no
 * behaviour.
 */
public class MeeshoPriceDetailsPageLocators {

    @AndroidFindBy(id = "com.meesho.supply:id/title_text")
    public WebElement title;

    @AndroidFindBy(id = "com.meesho.supply:id/txt_max_price")
    public WebElement mrpLabel;

    @AndroidFindBy(id = "com.meesho.supply:id/max_price")
    public WebElement mrpValue;

    @AndroidFindBy(id = "com.meesho.supply:id/txt_product_price")
    public WebElement productPriceLabel;

    @AndroidFindBy(id = "com.meesho.supply:id/product_price")
    public WebElement productPriceValue;

    @AndroidFindBy(id = "com.meesho.supply:id/txt_single_discount")
    public WebElement discountLabel;

    @AndroidFindBy(id = "com.meesho.supply:id/single_discount")
    public WebElement discountValue;

    @AndroidFindBy(id = "com.meesho.supply:id/txt_final_price")
    public WebElement finalPriceLabel;

    @AndroidFindBy(id = "com.meesho.supply:id/final_price")
    public WebElement finalPriceValue;

    @AndroidFindBy(id = "com.meesho.supply:id/tv_disclaimer")
    public WebElement disclaimer;

    @AndroidFindBy(id = "com.meesho.supply:id/primary_cta")
    public WebElement okButton;
}
