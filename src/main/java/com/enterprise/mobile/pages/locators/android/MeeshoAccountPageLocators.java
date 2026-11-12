package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoAccountPage}. Plain data class — no behaviour.
 *
 * <p>When logged out the header shows a "Sign up" button; when logged in it
 * shows the masked phone number and a "Logout" row at the bottom. Neither
 * "Sign up" nor "Logout" exposes a resource id, so text selectors are used.</p>
 */
public class MeeshoAccountPageLocators {

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Sign up\")")
    public WebElement signUp;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"ACCOUNT\")")
    public WebElement accountTitle;

    @AndroidFindBy(uiAutomator = "new UiSelector().textContains(\"+91\")")
    public WebElement phoneHeader;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Logout\")")
    public WebElement logout;

    @AndroidFindBy(id = "com.meesho.supply:id/btn_logout")
    public WebElement confirmLogout;

    @AndroidFindBy(id = "com.meesho.supply:id/btn_cancel")
    public WebElement cancelLogout;
}
