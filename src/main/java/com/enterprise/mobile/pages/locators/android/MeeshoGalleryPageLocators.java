package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoGalleryPage} (FullScreenViewPagerActivity).
 * Plain data class — no behaviour.
 */
public class MeeshoGalleryPageLocators {

    @AndroidFindBy(id = "com.meesho.supply:id/full_screen_image_view")
    public WebElement fullScreenImage;

    @AndroidFindBy(id = "com.meesho.supply:id/media_wrapper_viewpager")
    public WebElement mediaPager;

    @AndroidFindBy(id = "com.meesho.supply:id/horizontal_scroll_layout")
    public WebElement thumbnailStrip;

    @AndroidFindBy(id = "com.meesho.supply:id/real_images_ingress")
    public WebElement realImagesEntry;
}
