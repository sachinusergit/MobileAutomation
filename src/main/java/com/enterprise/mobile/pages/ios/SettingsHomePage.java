package com.enterprise.mobile.pages.ios;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.ios.SettingsHomePageLocators;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

/**
 * iOS Settings application. Reference implementation for iOS page objects
 * using XCUITest predicate locators. Locators live in
 * {@link com.enterprise.mobile.pages.locators.ios.SettingsHomePageLocators}.
 */
public class SettingsHomePage extends BasePage {

    private final SettingsHomePageLocators locators;

    public SettingsHomePage(AppiumDriver driver) {
        super(driver);
        locators = new SettingsHomePageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(locators.generalCell) || isDisplayed(locators.settingsList);
    }

    public boolean isSearchFieldDisplayed() {
        return isDisplayed(locators.searchField);
    }

    public SettingsHomePage search(String query) {
        log.info("Searching iOS settings for '{}'", query);
        click(locators.searchField);
        type(locators.searchField, query);
        return this;
    }

    /** Dynamic locator - cannot be a PageFactory field; kept as By. */
    public boolean isSearchResultDisplayed(String text) {
        By result = AppiumBy.iOSNsPredicateString(
                "label CONTAINS[c] '%s'".formatted(escapePredicate(text)));
        return wait.isPresent(result);
    }

    public GeneralSettingsPage openGeneral() {
        click(locators.generalCell);
        return new GeneralSettingsPage(driver);
    }

    private static String escapePredicate(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
