package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoListingPage} (catalog grid). Plain data class —
 * the product cards themselves are a dynamic list and are queried by id in the
 * page object.
 */
public class MeeshoListingPageLocators {

    @AndroidFindBy(id = "com.meesho.supply:id/catalog_recycler_view")
    public WebElement catalogRecycler;

    @AndroidFindBy(id = "com.meesho.supply:id/delivery_text")
    public WebElement deliveryText;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Filters\")")
    public WebElement filtersButton;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Sort\")")
    public WebElement sortButton;
}
