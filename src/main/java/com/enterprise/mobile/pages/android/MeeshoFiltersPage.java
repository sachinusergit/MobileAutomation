package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * "FILTERS" bottom sheet opened from the filter row of the home page. It lists
 * the filter sections on the left rail and the selectable options as pills on
 * the right. Every option exposes a {@code selected} attribute.
 */
public class MeeshoFiltersPage extends BasePage {

    private static final String SECTION_LABEL = "com.meesho.supply:id/tv_label";
    private static final String OPTION_PILL = "com.meesho.supply:id/filter2_value_pill";
    private static final String CONFIRM_CTA = "com.meesho.supply:id/primary_cta";

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MeeshoFiltersPage.class);

    private final java.util.Map<String, String> selectedFilters = new java.util.LinkedHashMap<>();

    /** The name of the filter section that is currently open. */
    private String currentSection;

    public MeeshoFiltersPage(AppiumDriver driver) {
        super(driver);
    }

    @Override
    public boolean isLoaded() {
        return isFilterPanelDisplayed();
    }

    /** True when the bottom sheet with the filters is on screen. */
    public boolean isFilterPanelDisplayed() {
        return !driver.findElements(AppiumBy.id(CONFIRM_CTA)).isEmpty()
                || !driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().text(\"FILTERS\")")).isEmpty();
    }

    /** True when the Home tab of the footer navigation is the active tab. */
    public boolean isHomeTabActive() {
        for (WebElement tab : driver.findElements(AppiumBy.id("com.meesho.supply:id/action_first_menu"))) {
            try {
                if ("true".equalsIgnoreCase(tab.getAttribute("selected"))) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Tab re-rendered; try the next one.
            }
        }
        return false;
    }

    /** Opens the filter panel by tapping the "Filters" entry of the filter row. */
    public MeeshoFiltersPage openFilterPanel() {
        log.info("Opening the filters panel");
        selectedFilters.clear();
        clickByScrolling(AppiumBy.androidUIAutomator("new UiSelector().text(\"Filters\")"));
        sleep(400);
        return this;
    }

    /** Selects the named filter section on the left rail. */
    public MeeshoFiltersPage openSection(String section) {
        log.info("Opening the {} filter section", section);
        currentSection = section;
        try {
            WebElement scrolled = driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true).setAsVerticalList())"
                            + ".setMaxSearchSwipes(8)"
                            + ".scrollIntoView(new UiSelector().text(\"" + section + "\"))"));
            clickForcefully(scrolled);
        } catch (RuntimeException e) {
            LOGGER.warn("UiScrollable could not reach {}: {}", section, e.getMessage());
            clickByScrolling(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + SECTION_LABEL + "\").text(\"" + section + "\")"));
        }
        sleep(400);
        return this;
    }

    /** Reads every option pill currently shown in the right pane. */
    public List<String> readOptions() {
        List<String> options = new ArrayList<>();
        for (WebElement pill : driver.findElements(AppiumBy.id(OPTION_PILL))) {
            try {
                if (!pill.isDisplayed()) {
                    continue;
                }
                String text = pill.getText().trim();
                if (!text.isEmpty() && !options.contains(text)) {
                    options.add(text);
                }
            } catch (RuntimeException ignored) {
                // Pill re-rendered; skip it.
            }
        }
        return options;
    }

    /** Selects the option of the current section, e.g. "Men" or "Orange". */
    public MeeshoFiltersPage selectOption(String option) {
        log.info("Selecting the {} option in the {} filter", option, currentSection);
        // Do not touch an option that is already selected: tapping it again
        // would unselect it.
        if (isOptionSelected(option)) {
            String section = currentSection == null ? "Unknown" : currentSection;
            selectedFilters.put(section, option);
            LOGGER.info("Filter already selected -> {} = {}", section, option);
            return this;
        }
        for (WebElement pill : driver.findElements(AppiumBy.id(OPTION_PILL))) {
            try {
                if (!pill.isDisplayed()) {
                    continue;
                }
                if (pill.getText().trim().equalsIgnoreCase(option)) {
                    clickForcefully(pill);
                    sleep(300);
                    boolean marked = "true".equalsIgnoreCase(pill.getAttribute("selected"));
                    if (!marked) {
                        // The pill may have re-rendered after the tap; probe it again.
                        marked = isOptionSelected(option);
                    }
                    String section = currentSection == null ? "Unknown" : currentSection;
                    if (marked) {
                        selectedFilters.put(section, option);
                        LOGGER.info("Filter selected -> {} = {}", section, option);
                    } else {
                        LOGGER.warn("Filter {} = {} could not be confirmed as selected", section,
                                option);
                    }
                    return this;
                }
            } catch (RuntimeException ignored) {
                // Pill re-rendered; check the next one.
            }
        }
        throw new IllegalStateException(
                "The filter option '" + option + "' was not found on screen");
    }

    /** True when the option pill is marked as selected. */
    public boolean isOptionSelected(String option) {
        for (WebElement pill : driver.findElements(AppiumBy.id(OPTION_PILL))) {
            try {
                if (pill.getText().trim().equalsIgnoreCase(option)) {
                    return "true".equalsIgnoreCase(pill.getAttribute("selected"));
                }
            } catch (RuntimeException ignored) {
                // Pill re-rendered; check the next one.
            }
        }
        return false;
    }

    /** Confirms the filters with the bottom-right "Done" button. */
    public MeeshoFiltersPage tapDone() {
        log.info("Tapping the Done button");
        clickByScrolling(AppiumBy.id(CONFIRM_CTA));
        sleep(500);
        if (selectedFilters.isEmpty()) {
            LOGGER.info("Applied filters: none");
        } else {
            LOGGER.info("Applied filters: {}",
                    String.join(" | ", selectedFilters.entrySet().stream()
                            .map(e -> e.getKey() + " = " + e.getValue())
                            .toList()));
        }
        return this;
    }

    /** The texts of the whole sheet, for diagnostics. */
    public List<String> visibleTexts() {
        List<String> texts = new ArrayList<>();
        for (WebElement element : driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().textMatches(\".+\")"))) {
            try {
                if (element.isDisplayed()) {
                    String text = element.getText().trim();
                    if (!text.isEmpty()) {
                        texts.add(text);
                    }
                }
            } catch (RuntimeException ignored) {
                // Element re-rendered; skip it.
            }
        }
        return texts;
    }
}
