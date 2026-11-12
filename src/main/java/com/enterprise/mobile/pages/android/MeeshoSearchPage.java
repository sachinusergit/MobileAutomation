package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Search bar at the top of the home page and the autosuggestion list shown
 * while typing. The suggestions are rendered as rows with the id
 * {@code list_item_title}.
 */
public class MeeshoSearchPage extends BasePage {

    private static final By SEARCH_BAR =
            AppiumBy.id("com.meesho.supply:id/query_edit_text");
    private static final By SUGGESTION =
            AppiumBy.id("com.meesho.supply:id/auto_complete_list_item");

    public MeeshoSearchPage(AppiumDriver driver) {
        super(driver);
    }

    @Override
    public boolean isLoaded() {
        return findVisible(SEARCH_BAR) != null || !driver.findElements(SUGGESTION).isEmpty();
    }

    /** True when the search field is on screen. */
    public boolean isSearchBarDisplayed() {
        return findVisible(SEARCH_BAR) != null;
    }

    /** Tapping the search bar opens the search state with suggestions. */
    public MeeshoSearchPage tapSearchBar() {
        log.info("Tapping the search bar");
        clickByScrolling(SEARCH_BAR);
        sleep(400);
        return this;
    }

    /** Clears the search field. */
    public MeeshoSearchPage clearSearchBar() {
        log.info("Clearing the search field");
        WebElement bar = findVisible(SEARCH_BAR);
        if (bar != null) {
            bar.clear();
            sleep(300);
        }
        return this;
    }

    /** Types the given query into the search bar. */
    public MeeshoSearchPage typeQuery(String query) {
        log.info("Typing '{}' into the search bar", query);
        WebElement bar = findVisible(SEARCH_BAR);
        if (bar == null) {
            throw new IllegalStateException("The search bar is not on screen");
        }
        bar.sendKeys(query);
        sleep(400);
        return this;
    }

    /** Reads every suggestion row currently on screen. */
    public List<String> readSuggestions() {
        List<WebElement> rows = driver.findElements(SUGGESTION);
        List<String> suggestions = new ArrayList<>();
        for (WebElement row : rows) {
            try {
                if (!row.isDisplayed()) {
                    continue;
                }
                String text = row.findElement(
                        AppiumBy.id("com.meesho.supply:id/list_item_title")).getText().trim();
                if (!text.isEmpty() && !suggestions.contains(text)) {
                    suggestions.add(text);
                }
            } catch (RuntimeException ignored) {
                // Row re-rendered; check the next one.
            }
        }
        return suggestions;
    }

    /** Selects the suggestion at the given zero-based index. */
    public MeeshoSearchPage selectSuggestion(int index) {
        log.info("Selecting suggestion index {}", index);
        List<WebElement> rows = driver.findElements(SUGGESTION).stream()
                .filter(this::isActuallyDisplayed)
                .toList();
        if (rows.size() <= index) {
            throw new IllegalStateException(
                    "Only " + rows.size() + " suggestions are on screen; index " + index
                            + " is not available");
        }
        WebElement chosen = rows.get(index);
        String text = chosen.findElement(
                AppiumBy.id("com.meesho.supply:id/list_item_title")).getText().trim();
        log.info("Tapping suggestion '{}'", text);
        clickForcefully(chosen);
        return this;
    }

    /** Text currently shown in the search bar. */
    public String getQueryText() {
        WebElement bar = findVisible(SEARCH_BAR);
        return bar == null ? "" : bar.getText().trim();
    }

    /** True when the results screen shows product cards. */
    public boolean areResultsDisplayed() {
        for (WebElement element : driver.findElements(
                AppiumBy.id("com.meesho.supply:id/item_catalog_card_optimised"))) {
            try {
                if (element.isDisplayed()) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Element re-rendered; check the next one.
            }
        }
        return false;
    }

    private WebElement findVisible(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (RuntimeException ignored) {
                // Element re-rendered; check the next one.
            }
        }
        return null;
    }

    private boolean isActuallyDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
