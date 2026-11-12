package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoProductPage} (SingleProductActivity). Plain data
 * class — size pills are a dynamic list queried by id in the page object.
 */
public class MeeshoProductPageLocators {

    @AndroidFindBy(id = "com.meesho.supply:id/product_name_text_with_brand")
    public WebElement productName;

    @AndroidFindBy(id = "com.meesho.supply:id/media_wrapper_viewpager")
    public WebElement mediaPager;

    @AndroidFindBy(id = "com.meesho.supply:id/product_image")
    public WebElement productImage;

    @AndroidFindBy(id = "com.meesho.supply:id/indicator")
    public WebElement imageIndicator;

    @AndroidFindBy(id = "com.meesho.supply:id/img_right_chevron")
    public WebElement nextImage;

    @AndroidFindBy(id = "com.meesho.supply:id/price_text")
    public WebElement priceText;

    @AndroidFindBy(id = "com.meesho.supply:id/original_price_text")
    public WebElement originalPriceText;

    @AndroidFindBy(id = "com.meesho.supply:id/discount_text")
    public WebElement discountText;

    @AndroidFindBy(id = "com.meesho.supply:id/onwards_label")
    public WebElement onwardsLabel;

    @AndroidFindBy(id = "com.meesho.supply:id/price_icon_info")
    public WebElement priceInfoIcon;

    @AndroidFindBy(id = "com.meesho.supply:id/selectSize")
    public WebElement selectSizeLabel;

    @AndroidFindBy(id = "com.meesho.supply:id/size_chart_title")
    public WebElement sizeChart;

    @AndroidFindBy(id = "com.meesho.supply:id/primary_cta")
    public WebElement buyNow;

    @AndroidFindBy(id = "com.meesho.supply:id/text_total_price")
    public WebElement buyNowSheetTotal;
}
