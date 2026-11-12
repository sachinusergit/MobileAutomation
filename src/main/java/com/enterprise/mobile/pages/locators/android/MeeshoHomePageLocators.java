package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoHomePage} (footer navigation and header chrome).
 * Plain data class — no behaviour.
 */
public class MeeshoHomePageLocators {

    @AndroidFindBy(id = "com.meesho.supply:id/action_account")
    public WebElement accountTab;

    /**
     * Header profile avatar. Meesho ships two footer layouts — a reseller one
     * with an "Account" tab and a consumer one (with "Mall") where the profile
     * is reached from this avatar instead.
     */
    @AndroidFindBy(id = "com.meesho.supply:id/default_profile_image")
    public WebElement profileAvatar;

    @AndroidFindBy(id = "com.meesho.supply:id/action_categories")
    public WebElement categoriesTab;

    @AndroidFindBy(id = "com.meesho.supply:id/action_first_menu")
    public WebElement homeTab;

    @AndroidFindBy(id = "com.meesho.supply:id/action_orders")
    public WebElement ordersTab;

    @AndroidFindBy(id = "com.meesho.supply:id/cart_count")
    public WebElement cartCount;

    @AndroidFindBy(id = "com.meesho.supply:id/query_edit_text")
    public WebElement searchField;
}
