package com.enterprise.mobile.pages.locators.ios;

import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code com.enterprise.mobile.pages.ios.SettingsHomePage}.
 * Plain data class — no behaviour.
 */
public class SettingsHomePageLocators {

    @iOSXCUITFindBy(iOSNsPredicate = "type == 'XCUIElementTypeSearchField'")
    public WebElement searchField;

    @iOSXCUITFindBy(iOSNsPredicate = "label == 'General' AND type == 'XCUIElementTypeCell'")
    public WebElement generalCell;

    @iOSXCUITFindBy(iOSNsPredicate = "type == 'XCUIElementTypeTable'")
    public WebElement settingsList;
}
