package com.enterprise.mobile.pages.locators.android;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Locators for {@code MeeshoSignUpPage}. Plain data class — no behaviour.
 *
 * <p>Three layers can appear after tapping "Sign up":</p>
 * <ul>
 *   <li>Google Credential Manager account picker (dismiss with NONE OF THE ABOVE)</li>
 *   <li>Truecaller "Login to Meesho" sheet with the saved name/number and a
 *   CONTINUE button plus an ENTER DETAILS MANUALLY fallback</li>
 *   <li>Meesho's native phone-number screen ("Sign up to continue")</li>
 * </ul>
 */
public class MeeshoSignUpPageLocators {

    // --- Google credential picker -------------------------------------------------

    @AndroidFindBy(id = "com.google.android.gms:id/cancel")
    public WebElement googlePickerDismiss;

    @AndroidFindBy(id = "com.google.android.gms:id/credential_picker_layout")
    public WebElement googlePickerLayout;

    // --- Truecaller "Login to Meesho" sheet --------------------------------------

    @AndroidFindBy(id = "com.truecaller:id/tv_partner_name")
    public WebElement loginDialogTitle;

    @AndroidFindBy(id = "com.truecaller:id/tv_user_name")
    public WebElement loginDialogName;

    @AndroidFindBy(id = "com.truecaller:id/tv_user_number")
    public WebElement loginDialogNumber;

    @AndroidFindBy(id = "com.truecaller:id/tv_confirm")
    public WebElement truecallerContinue;

    @AndroidFindBy(id = "com.truecaller:id/tv_continueWithDifferentNumber")
    public WebElement enterDetailsManually;

    @AndroidFindBy(id = "com.truecaller:id/tv_terms_privacy")
    public WebElement truecallerTerms;

    // --- Meesho native phone screen ----------------------------------------------

    @AndroidFindBy(id = "com.meesho.supply:id/contextual_title")
    public WebElement nativeTitle;

    @AndroidFindBy(id = "com.meesho.supply:id/country_code")
    public WebElement countryCode;

    @AndroidFindBy(id = "com.meesho.supply:id/phone_edit_text")
    public WebElement phoneNumberField;

    @AndroidFindBy(id = "com.meesho.supply:id/next_button")
    public WebElement continueButton;

    @AndroidFindBy(id = "com.meesho.supply:id/instructions_text")
    public WebElement termsText;
}
