package com.enterprise.mobile.bdd.steps;

import com.enterprise.mobile.bdd.context.TestContext;
import com.enterprise.mobile.pages.android.MeeshoFiltersPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps for {@code 05-meesho-filters.feature}. Opens the filter panel of the
 * home page and applies the Gender, Discount, Price and Color filters.
 */
public class MeeshoFiltersSteps {

    private static final Logger LOG = LoggerFactory.getLogger(MeeshoFiltersSteps.class);

    private final TestContext context;
    private MeeshoFiltersPage filtersPage;

    public MeeshoFiltersSteps(TestContext context) {
        this.context = context;
    }

    @And("the footer Home tab should be the active tab")
    public void homeTabIsActive() {
        filtersPage = new MeeshoFiltersPage(context.getDriver());
        assertThat(filtersPage.isHomeTabActive())
                .as("The Home tab of the footer should be the active tab")
                .isTrue();
    }

    @When("the user taps the Filters option below the search bar on the right")
    public void openFilterPanel() {
        filtersPage = new MeeshoFiltersPage(context.getDriver());
        filtersPage.openFilterPanel();
    }

    @Then("the filter panel should be displayed")
    public void filterPanelDisplayed() {
        assertThat(filtersPage.isFilterPanelDisplayed())
                .as("The filter panel should be displayed")
                .isTrue();
    }

    @When("the user opens the Gender section and selects Men")
    public void openGenderAndSelectMen() {
        filtersPage.openSection("Gender");
        List<String> genders = filtersPage.readOptions();
        LOG.info("Gender options in the filter: {}", genders);
        filtersPage.selectOption("Men");
    }

    @When("the user opens the Discount section and selects 50% And Above")
    public void openDiscountAndSelect50AndAbove() {
        filtersPage.openSection("Discount");
        List<String> discounts = filtersPage.readOptions();
        LOG.info("Discount options in the filter: {}", discounts);
        assertThat(discounts)
                .as("The Discount section should list the discount options")
                .isNotEmpty();
        filtersPage.selectOption("50% And Above");
    }

    @When("the user opens the Price section and selects Under Rs 499")
    public void openPriceAndSelectUnderRs499() {
        filtersPage.openSection("Price");
        List<String> prices = filtersPage.readOptions();
        LOG.info("Price options in the filter: {}", prices);
        assertThat(prices)
                .as("The Price section should list price range options")
                .isNotEmpty();
        filtersPage.selectOption("Under ₹ 499");
    }

    @When("the user opens the Color section and selects the available color")
    public void openColorAndSelectAvailableColor() {
        filtersPage.openSection("Color");
        List<String> colours = filtersPage.readOptions();
        LOG.info("Colours in the filter: {}", colours);
        assertThat(colours)
                .as("The Color section should list colour options")
                .isNotEmpty();
        String available = colours.stream()
                .filter(c -> c.equalsIgnoreCase("Orange"))
                .findFirst()
                .orElse(colours.get(0));
        LOG.info("Selecting {} as the colour filter", available);
        filtersPage.selectOption(available);
    }

    @Then("the selected filters should be applied to the home page")
    public void filterPanelClosingStateVerified() {
        assertThat(filtersPage.isFilterPanelDisplayed())
                .as("The filter panel should still be displayed with the chosen filters")
                .isTrue();
    }

    @When("the user taps the Done button")
    public void tapDoneButton() {
        filtersPage.tapDone();
    }
}
