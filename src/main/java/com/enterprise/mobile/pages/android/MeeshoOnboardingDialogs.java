package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The pop-ups Meesho shows on top of the app: the language chooser and the
 * gender chooser. They appear after the account flow and block every following
 * step, so they are answered (English / Male) as soon as they show up - they
 * are never simply closed.
 */
public class MeeshoOnboardingDialogs extends BasePage {

    /** The English row of the language chooser (Meesho labels it in caps). */
    private static final By ENGLISH_OPTION = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"ENGLISH\")");
    private static final By ENGLISH_TEXT = AppiumBy.androidUIAutomator(
            "new UiSelector().textMatches(\"(?i)^english$\")");
    private static final By LANGUAGE_MARKER = AppiumBy.androidUIAutomator(
            "new UiSelector().textMatches(\"(?i)^(select language|choose language|"
                    + "select your language|language selection)$\")");

    /** The gender chooser always offers both options. */
    private static final By MALE_OPTION = AppiumBy.androidUIAutomator(
            "new UiSelector().textMatches(\"(?i)^male$\")");
    private static final By FEMALE_OPTION = AppiumBy.androidUIAutomator(
            "new UiSelector().textMatches(\"(?i)^female$\")");
    private static final By GENDER_MARKER = AppiumBy.androidUIAutomator(
            "new UiSelector().textMatches(\"(?i)^(select gender|choose gender|"
                    + "what'?s your gender\\?|gender selection)$\")");

    private static final By CONFIRM_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().textMatches(\"(?i)^(confirm|done|save|continue|ok|submit|apply)$\")");

    public MeeshoOnboardingDialogs(AppiumDriver driver) {
        super(driver);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    /**
     * Answers the pop-ups that are on screen: English for the language chooser
     * and Male for the gender chooser.
     *
     * @return {@code true} when a pop-up was answered
     */
    public boolean dismissVisibleDialogs() {
        boolean handled = false;
        handled |= chooseEnglishIfShown();
        handled |= chooseMaleIfShown();
        return handled;
    }

    /** True when the language chooser is on screen. */
    public boolean isLanguageDialogDisplayed() {
        // Only the chooser itself counts - a plain "English" text on a product
        // page must not be mistaken for it.
        return !driver.findElements(ENGLISH_OPTION).isEmpty()
                || !driver.findElements(LANGUAGE_MARKER).isEmpty();
    }

    /**
     * True when the gender chooser is on screen. It is only the chooser when
     * both options are shown - the "Gender" filter of the listing page and the
     * "Men" category must never be mistaken for it.
     */
    public boolean isGenderDialogDisplayed() {
        boolean male = !driver.findElements(MALE_OPTION).isEmpty();
        boolean female = !driver.findElements(FEMALE_OPTION).isEmpty();
        return (male && female) || !driver.findElements(GENDER_MARKER).isEmpty();
    }

    /** Picks English on the language chooser. */
    public boolean chooseEnglishIfShown() {
        if (!isLanguageDialogDisplayed()) {
            return false;
        }
        log.info("A language dialog is on screen; choosing English");
        if (!clickFirst(ENGLISH_OPTION)) {
            clickFirst(ENGLISH_TEXT);
        }
        confirmIfShown();
        return true;
    }

    /** Picks Male on the gender chooser. */
    public boolean chooseMaleIfShown() {
        if (!isGenderDialogDisplayed()) {
            return false;
        }
        log.info("A gender dialog is on screen; choosing Male");
        dumpPageSource("gender-dialog");
        clickFirst(MALE_OPTION);
        confirmIfShown();
        return true;
    }

    /** Writes the current screen to target/screenshots/debug for analysis. */
    private void dumpPageSource(String tag) {
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get("target", "screenshots", "debug");
            java.nio.file.Files.createDirectories(dir);
            String name = tag + "-" + System.currentTimeMillis() + ".xml";
            java.nio.file.Files.write(dir.resolve(name),
                    driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            log.info("Page source dumped to target/screenshots/debug/{}", name);
        } catch (Exception e) {
            log.warn("Could not dump the page source: {}", e.getMessage());
        }
    }

    private void confirmIfShown() {
        for (WebElement button : driver.findElements(CONFIRM_BUTTON)) {
            try {
                if (button.isDisplayed()) {
                    clickForcefully(button);
                    return;
                }
            } catch (RuntimeException ignored) {
                // Button re-rendered; try the next one.
            }
        }
    }

    private boolean clickFirst(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    clickForcefully(element);
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Element re-rendered; try the next one.
            }
        }
        return false;
    }

    /** The texts of the dialog currently on screen (for diagnostics). */
    public List<String> visibleDialogTexts() {
        List<String> texts = new java.util.ArrayList<>();
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
