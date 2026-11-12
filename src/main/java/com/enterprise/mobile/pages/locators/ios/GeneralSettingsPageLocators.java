package com.enterprise.mobile.pages.locators.ios;

import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code com.enterprise.mobile.pages.ios.GeneralSettingsPage}.
 * Plain data class — no behaviour.
 */
public class GeneralSettingsPageLocators {

    @iOSXCUITFindBy(iOSNsPredicate = "label == 'About' AND type == 'XCUIElementTypeCell'")
    public WebElement aboutCell;

    @iOSXCUITFindBy(iOSNsPredicate = "label == 'General'")
    public WebElement title;
}
