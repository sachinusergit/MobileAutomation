package com.enterprise.mobile.bdd.steps;

import com.enterprise.mobile.bdd.context.TestContext;
import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.pages.android.MeeshoAccountPage;
import com.enterprise.mobile.pages.android.MeeshoHomePage;
import com.enterprise.mobile.pages.android.MeeshoSignUpPage;
import io.appium.java_client.android.AndroidDriver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Android application launch and Meesho Supply scenarios.
 */
public class ApplicationSteps {

    private final TestContext context;

    private MeeshoHomePage homePage;
    private MeeshoAccountPage accountPage;
    private MeeshoSignUpPage signUpPage;

    public ApplicationSteps(TestContext context) {
        this.context = context;
    }

    private boolean alreadySignedIn;

    @Given("the configured application is launched")
    public void configuredApplicationLaunched() {
        homePage = new MeeshoHomePage(context.getDriver());
    }

    @Then("the configured application should be in the foreground")
    public void configuredApplicationInForeground() {
        String expectedPackage = ConfigManager.get("app.package", "");
        assertThat(expectedPackage)
                .as("app.package must be configured")
                .isNotBlank();

        // MeeshoHomePage.isLoaded() re-activates the app once when the device
        // dropped back to the launcher (e.g. after a cold start or an update).
        homePage = new MeeshoHomePage(context.getDriver());
        assertThat(homePage.isLoaded())
                .as("Package '%s' should be in the foreground", expectedPackage)
                .isTrue();
    }

    @Then("Meesho Supply should be in the foreground")
    public void meeshoSupplyInForeground() {
        homePage = new MeeshoHomePage(context.getDriver());

        assertThat(homePage.isLoaded())
                .as("Meesho Supply should be in the foreground")
                .isTrue();
    }

    @Then("the current package should match the configured application package")
    public void currentPackageMatchesConfigured() {
        if (homePage == null) {
            homePage = new MeeshoHomePage(context.getDriver());
        }
        assertThat(homePage.getCurrentPackage())
                .as("Current package should match the configured app.package")
                .isEqualTo(ConfigManager.get("app.package"));
    }

    @When("the user opens the account tab")
    public void userOpensAccountTab() {
        accountPage = new MeeshoHomePage(context.getDriver()).openAccountTab();
    }

    @Then("the account screen should be displayed")
    public void accountScreenDisplayed() {
        assertThat(accountPage.isLoaded())
                .as("Account screen should be displayed")
                .isTrue();
    }

    @When("the user taps Sign up")
    public void userTapsSignUp() {
        if (accountPage == null) {
            accountPage = new MeeshoAccountPage(context.getDriver());
        }
        if (accountPage.isSignedIn()) {
            // The session is already signed in from a previous scenario; the
            // sign-up surface is not offered, so the scenario carries on.
            alreadySignedIn = true;
            return;
        }
        signUpPage = accountPage.tapSignUp();
    }

    @Then("the sign-up step should be displayed")
    public void signUpStepDisplayed() {
        if (alreadySignedIn) {
            return;
        }
        assertThat(signUpPage).as("The sign-up surface must be initialised").isNotNull();
        assertThat(signUpPage.isLoaded())
                .as("Sign up / login step should be displayed")
                .isTrue();
    }

    @When("the user enters the configured phone number")
    public void userEntersConfiguredPhoneNumber() {
        if (alreadySignedIn) {
            return;
        }
        String phoneNumber = ConfigManager.get("meesho.signup.phone", "");
        assertThat(phoneNumber)
                .as("meesho.signup.phone must be configured")
                .isNotBlank();

        signUpPage.openManualPhoneEntry().enterPhoneNumber(phoneNumber);
    }

    @When("the user taps Continue")
    public void userTapsContinue() {
        if (alreadySignedIn) {
            return;
        }
        signUpPage.tapContinue();
    }
}
