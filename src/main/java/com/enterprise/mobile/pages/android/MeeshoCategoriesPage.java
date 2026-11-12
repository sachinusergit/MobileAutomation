package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoCategoriesPageLocators;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * Meesho "Categories" tab: a left rail of top-level categories and a right
 * pane of sub-categories. Opening Men shows section headers such as
 * "Bottom Wear" with the "Cargos/Trousers" entry beneath it.
 */
public class MeeshoCategoriesPage extends BasePage {

    private final MeeshoCategoriesPageLocators locators;

    public MeeshoCategoriesPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoCategoriesPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        return awaitAnyDisplayed(20, locators.categoriesTitle);
    }

    /**
     * Quick check (without waiting) whether the categories screen is the one
     * on top - used to repeat the categories steps from the categories screen.
     */
    public boolean isOnCategoriesScreen() {
        return isDisplayed(locators.categoriesTitle);
    }

    public MeeshoCategoriesPage openMenCategory() {
        log.info("Opening the Men category");
        clickByScrolling(io.appium.java_client.AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"Men\")"));
        return this;
    }

    /** The "Bottom Wear" section header must be visible before opening Cargos/Trousers. */
    public boolean isBottomWearSectionDisplayed() {
        return isDisplayed(locators.bottomWearHeader);
    }

    /** True when the left category rail shows the top-level entries (for example Men). */
    public boolean isCategoryRailDisplayed() {
        if (isDisplayed(locators.menCategory)) {
            return true;
        }
        // Any of the well known rail entries proves the rail is on screen.
        for (String entry : new String[] {"Men", "Women", "Home & Kitchen", "Beauty", "Electronics"}) {
            if (!driver.findElements(io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"" + entry + "\")")).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public MeeshoListingPage openCargosAndTrousers() {
        for (int attempt = 0; attempt < 3 && !isDisplayed(locators.cargosTrousers); attempt++) {
            swipeUp();
        }
        clickForcefully(locators.cargosTrousers);
        return new MeeshoListingPage(driver);
    }
}
