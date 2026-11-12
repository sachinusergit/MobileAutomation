package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoHomePageLocators;
import com.enterprise.mobile.utils.GestureUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;

/**
 * Meesho Supply home screen. The screen's locators live in
 * {@link MeeshoHomePageLocators}; this class keeps the behaviour.
 */
public class MeeshoHomePage extends BasePage {

    private final MeeshoHomePageLocators locators;
    private final String expectedPackage = ConfigManager.get("app.package", "com.meesho.supply");

    public MeeshoHomePage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoHomePageLocators();
        PageFactory.initElements(decorator, locators);
    }

    public MeeshoAccountPage openAccountTab() {
        // The account header only exists on the Home tab: listings, product
        // pages and the other tabs show a search/cart header instead.
        for (int attempt = 0; attempt < 8; attempt++) {
            if (isDisplayed(locators.accountTab) || isDisplayed(locators.profileAvatar)) {
                break;
            }
            if (isDisplayed(locators.homeTab)) {
                log.info("Selecting the Home tab before opening the account entry");
                tap(locators.homeTab);
                sleep(200);
                continue;
            }
            log.info("Account entry not on screen; navigating back towards home");
            driver.navigate().back();
            sleep(200);
        }
        if (isDisplayed(locators.accountTab)) {
            tap(locators.accountTab);
        } else {
            log.info("Footer has no Account tab (consumer layout); opening the header profile avatar");
            GestureUtils.tap(driver, wait.untilVisible(locators.profileAvatar));
        }
        return new MeeshoAccountPage(driver);
    }

    public MeeshoHomePage openHomeTab() {
        clickByScrolling(io.appium.java_client.AppiumBy.id(
                "com.meesho.supply:id/action_first_menu"));
        return this;
    }

    public MeeshoCategoriesPage openCategoriesTab() {
        clickByScrolling(io.appium.java_client.AppiumBy.id(
                "com.meesho.supply:id/action_categories"));
        return new MeeshoCategoriesPage(driver);
    }

    public boolean isFooterDisplayed() {
        return isAnyDisplayed(locators.accountTab, locators.categoriesTab, locators.homeTab);
    }

    @Override
    public boolean isLoaded() {
        if (awaitPackage(5)) {
            return true;
        }
        log.warn("{} is not in the foreground; re-activating the app", expectedPackage);
        driver.executeScript("mobile: activateApp", Map.of("appId", expectedPackage));
        return awaitPackage(20);
    }

    private boolean awaitPackage(int seconds) {
        WebDriverWait packageWait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        packageWait.pollingEvery(Duration.ofMillis(500));
        try {
            return Boolean.TRUE.equals(packageWait.until(webDriver -> expectedPackage.equalsIgnoreCase(
                    ((AndroidDriver) webDriver).getCurrentPackage())));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getCurrentPackage() {
        return androidDriver().getCurrentPackage();
    }

    public String getCurrentActivity() {
        return androidDriver().currentActivity();
    }

    private AndroidDriver androidDriver() {
        if (driver instanceof AndroidDriver androidDriver) {
            return androidDriver;
        }
        throw new IllegalStateException("MeeshoHomePage requires an AndroidDriver");
    }
}
