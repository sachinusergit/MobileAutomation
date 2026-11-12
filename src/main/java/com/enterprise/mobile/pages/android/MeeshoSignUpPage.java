package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoSignUpPageLocators;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * Sign-up / login step reached from the Account tab.
 *
 * <p>The screen can surface three different layers depending on device state:
 * the Google credential picker, the Truecaller "Login to Meesho" sheet, or
 * Meesho's native phone-number screen. Each helper tolerates the layers that
 * are absent.</p>
 */
public class MeeshoSignUpPage extends BasePage {

    private final MeeshoSignUpPageLocators locators;

    public MeeshoSignUpPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoSignUpPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return awaitAnyDisplayed(20, locators.loginDialogTitle, locators.nativeTitle,
                locators.googlePickerLayout);
    }

    /** Dismisses the Android Google Credential Manager sheet if it appears. */
    public MeeshoSignUpPage dismissGooglePickerIfPresent() {
        if (isDisplayed(locators.googlePickerDismiss)) {
            log.info("Dismissing Google credential picker with NONE OF THE ABOVE");
            tap(locators.googlePickerDismiss);
        }
        return this;
    }

    // --- Truecaller "Login to Meesho" sheet --------------------------------------

    public boolean isLoginDialogDisplayed() {
        return isDisplayed(locators.loginDialogTitle);
    }

    public String getLoginDialogTitle() {
        return textOf(locators.loginDialogTitle);
    }

    public String getLoginDialogName() {
        return textOf(locators.loginDialogName);
    }

    public String getLoginDialogNumber() {
        return textOf(locators.loginDialogNumber);
    }

    public void tapTruecallerContinue() {
        tap(locators.truecallerContinue);
    }

    public boolean isEnterDetailsManuallyDisplayed() {
        return isDisplayed(locators.enterDetailsManually);
    }

    public void tapEnterDetailsManually() {
        tap(locators.enterDetailsManually);
    }

    // --- Meesho native phone screen ----------------------------------------------

    public boolean isNativePhoneScreenDisplayed() {
        return isDisplayed(locators.phoneNumberField);
    }

    /**
     * Dismisses Truecaller's OAuth consent screen or the Google credential
     * picker when one of them has taken over the foreground. Meesho may hand
     * off to Truecaller automatically, which hides every Meesho element and
     * makes the next lookup wait for its whole timeout.
     */
    public MeeshoSignUpPage dismissThirdPartyLoginIfPresent() {
        if (driver instanceof AndroidDriver android) {
            String current = android.getCurrentPackage();
            boolean stray = current != null
                    && (current.startsWith("com.truecaller") || current.startsWith("com.google.android.gms"));
            if (stray) {
                log.info("Third-party login activity '{}' is in front; pressing Back", current);
                driver.navigate().back();
                sleep(150);
            }
        }
        return this;
    }

    /**
     * Waits for any sign-up surface to settle and navigates to Meesho's native
     * phone-number entry: dismisses the Google credential picker and, when the
     * Truecaller sheet is shown, chooses "Enter details manually".
     */
    public MeeshoSignUpPage openManualPhoneEntry() {
        long deadline = System.currentTimeMillis() + 45_000L;
        while (System.currentTimeMillis() < deadline) {
            dismissThirdPartyLoginIfPresent();
            dismissGooglePickerIfPresent();
            if (isNativePhoneScreenDisplayed()) {
                return this;
            }
            if (isEnterDetailsManuallyDisplayed()) {
                log.info("Truecaller sheet shown; choosing Enter details manually");
                tapEnterDetailsManually();
            }
            sleep(150);
        }
        return this;
    }

    public void enterPhoneNumber(String phoneNumber) {
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith("0") && digits.length() > 10) {
            digits = digits.substring(1);
        }
        if (digits.startsWith("91") && digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }
        type(locators.phoneNumberField, digits);
        log.info("Entered phone number {}", digits);
    }

    public void tapContinue() {
        tap(locators.continueButton);
    }
}
