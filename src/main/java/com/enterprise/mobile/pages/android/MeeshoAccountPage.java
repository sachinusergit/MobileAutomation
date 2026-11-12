package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoAccountPageLocators;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Meesho "Account" tab. Handles both the logged-out header ("Sign up") and the
 * logged-in header (masked number + "Logout" at the bottom of the list).
 */
public class MeeshoAccountPage extends BasePage {

    private final MeeshoAccountPageLocators locators;

    public MeeshoAccountPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoAccountPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return awaitAnyDisplayed(20, locators.accountTitle, locators.signUp, locators.logout);
    }

    public boolean isSignedOut() {
        return isDisplayed(locators.signUp);
    }

    /**
     * True when the masked phone number is on screen. The account header can
     * take a moment to render, so this polls briefly before giving up.
     */
    public boolean isSignedIn() {
        WebDriverWait headerWait = new WebDriverWait(driver, Duration.ofSeconds(6));
        headerWait.pollingEvery(Duration.ofMillis(300));
        try {
            return Boolean.TRUE.equals(headerWait.until(d -> {
                if (isDisplayed(locators.phoneHeader)) {
                    return true;
                }
                for (WebElement element : driver.findElements(
                        AppiumBy.androidUIAutomator("new UiSelector().textContains(\"+91\")"))) {
                    try {
                        if (element.isDisplayed()) {
                            return true;
                        }
                    } catch (RuntimeException ignored) {
                        // Element re-rendered; try the next one.
                    }
                }
                return false;
            }));
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    /** Direct probe for any sign-up / login surface on the account screen. */
    public boolean isSignUpDisplayed() {
        if (isDisplayed(locators.signUp)) {
            return true;
        }
        for (WebElement element : driver.findElements(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textMatches(\"(?i)^(sign ?up|login|register)$\")"))) {
            try {
                if (element.isDisplayed()) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Element re-rendered; try the next one.
            }
        }
        return false;
    }

    public MeeshoSignUpPage tapSignUp() {
        if (isDisplayed(locators.signUp)) {
            clickForcefully(locators.signUp);
        } else {
            clickByScrolling(AppiumBy.androidUIAutomator(
                    "new UiSelector().textMatches(\"(?i)^(sign ?up|login|register)$\")"));
        }
        return new MeeshoSignUpPage(driver);
    }

    /**
     * Ensures the account is signed out, scrolling the account list to reveal
     * the Logout row when necessary. When the row cannot be reached the
     * account is left untouched - the sign-up journey then continues with the
     * session that is already signed in.
     */
    public MeeshoAccountPage logoutIfSignedIn() {
        if (!isSignedIn()) {
            log.info("Account is already signed out");
            return this;
        }
        for (int attempt = 0; attempt < 20 && !isLogoutVisible(); attempt++) {
            swipeUp();
        }
        if (!isLogoutVisible()) {
            log.info("The Logout row could not be reached; keeping the current session");
            return this;
        }
        clickByScrolling(AppiumBy.androidUIAutomator(
                "new UiSelector().textMatches(\"(?i)^(log ?out|sign out)$\")"));
        if (isDisplayed(locators.confirmLogout)) {
            clickForcefully(locators.confirmLogout);
        }
        wait.untilVisibleWithin(java.time.Duration.ofSeconds(8), locators.signUp);
        log.info("Logged out of Meesho");
        return this;
    }

    /** True when the Logout row is on screen. */
    private boolean isLogoutVisible() {
        if (isDisplayed(locators.logout)) {
            return true;
        }
        if (!driver.findElements(AppiumBy.androidUIAutomator(
                "new UiSelector().textMatches(\"(?i)^(log ?out|sign out)$\")")).isEmpty()) {
            return true;
        }
        return !driver.findElements(AppiumBy.id("com.meesho.supply:id/btn_logout")).isEmpty();
    }
}
