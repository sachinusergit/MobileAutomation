package com.enterprise.mobile.bdd.steps;

import com.enterprise.mobile.bdd.context.TestContext;
import com.enterprise.mobile.pages.android.MeeshoSearchPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps for {@code 06-meesho-search.feature}. Searches for "Men shirts" and
 * selects the fifth autosuggestion.
 */
public class MeeshoSearchSteps {

    private static final Logger LOG = LoggerFactory.getLogger(MeeshoSearchSteps.class);

    private final TestContext context;
    private MeeshoSearchPage searchPage;

    public MeeshoSearchSteps(TestContext context) {
        this.context = context;
    }

    @When("the user taps the search bar on the home page")
    public void tapSearchBar() {
        searchPage = new MeeshoSearchPage(context.getDriver());
        searchPage.tapSearchBar();
    }

    @When("the user clears the search bar and types {string}")
    public void clearAndTypeQuery(String query) {
        searchPage = new MeeshoSearchPage(context.getDriver());
        searchPage.clearSearchBar();
        searchPage.typeQuery(query);
        assertThat(searchPage.getQueryText().toLowerCase())
                .as("The search bar should contain the typed query")
                .contains(query.toLowerCase());
    }

    @Then("the autosuggestions should be displayed")
    public void suggestionsDisplayed() {
        assertThat(searchPage.readSuggestions())
                .as("The suggestion list should not be empty")
                .isNotEmpty();
    }

    @And("the user reads the autosuggestions")
    public void readSuggestions() {
        List<String> suggestions = searchPage.readSuggestions();
        LOG.info("Autosuggestions: {}", suggestions);
        assertThat(suggestions)
                .as("All suggestions should be logged and listed")
                .isNotEmpty();
    }

    @When("the user selects the fifth autosuggestion")
    public void selectFifthSuggestion() {
        List<String> suggestions = searchPage.readSuggestions();
        LOG.info("Suggestion to select: index 4 of {}", suggestions);
        assertThat(suggestions.size())
                .as("At least five suggestions should be visible")
                .isGreaterThanOrEqualTo(5);
        LOG.info("Selected suggestion: {}", suggestions.get(4));
        searchPage.selectSuggestion(4);
    }

    @Then("the search results should be displayed for the selection")
    public void resultsDisplayed() {
        assertThat(searchPage.areResultsDisplayed())
                .as("The results page should display products")
                .isTrue();
    }
}
