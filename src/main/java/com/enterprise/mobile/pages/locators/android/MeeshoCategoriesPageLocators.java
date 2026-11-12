package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoCategoriesPage} (left rail + right pane).
 * Plain data class — no behaviour.
 */
public class MeeshoCategoriesPageLocators {

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"CATEGORIES\")")
    public WebElement categoriesTitle;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Men\")")
    public WebElement menCategory;

    @AndroidFindBy(uiAutomator = "new UiSelector().textContains(\"Bottom Wear\")")
    public WebElement bottomWearHeader;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Cargos/Trousers\")")
    public WebElement cargosTrousers;

    @AndroidFindBy(id = "com.meesho.supply:id/title_tv")
    public WebElement paneTitle;
}
