package com.enterprise.mobile.pages.ios;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.ios.GeneralSettingsPageLocators;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * iOS Settings -&gt; General screen.
 */
public class GeneralSettingsPage extends BasePage {

    private final GeneralSettingsPageLocators locators;

    public GeneralSettingsPage(AppiumDriver driver) {
        super(driver);
        locators = new GeneralSettingsPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(locators.aboutCell) || isDisplayed(locators.title);
    }

    public boolean isAboutOptionDisplayed() {
        return isDisplayed(locators.aboutCell);
    }
}
