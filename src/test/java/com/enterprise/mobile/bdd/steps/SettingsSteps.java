package com.enterprise.mobile.bdd.steps;

import com.enterprise.mobile.bdd.context.TestContext;
import com.enterprise.mobile.pages.ios.GeneralSettingsPage;
import com.enterprise.mobile.pages.ios.SettingsHomePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * iOS Settings scenarios. The Android Settings demo has been removed; these
 * steps back the XCUITest reference feature only.
 */
public class SettingsSteps {

    private final TestContext context;

    private SettingsHomePage iosHome;
    private GeneralSettingsPage generalPage;

    public SettingsSteps(TestContext context) {
        this.context = context;
    }

    @Given("the iOS Settings home screen is displayed")
    public void iosSettingsHomeDisplayed() {
        iosHome = new SettingsHomePage(context.getDriver());
        assertThat(iosHome.isLoaded())
                .as("iOS Settings home screen should be displayed")
                .isTrue();
    }

    @When("the user searches for {string} in settings")
    public void userSearchesFor(String query) {
        iosHome.search(query);
    }

    @Then("the settings results should contain {string}")
    public void settingsResultsContain(String expected) {
        assertThat(iosHome.isSearchResultDisplayed(expected))
                .as("Settings search results should contain '%s'", expected)
                .isTrue();
    }

    @When("the user opens General settings")
    public void userOpensGeneralSettings() {
        generalPage = iosHome.openGeneral();
    }

    @Then("the General settings screen should be displayed")
    public void generalSettingsDisplayed() {
        assertThat(generalPage.isLoaded())
                .as("General settings screen should be displayed")
                .isTrue();
    }
}
