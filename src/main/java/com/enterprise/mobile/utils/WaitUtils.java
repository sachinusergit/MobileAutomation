package com.enterprise.mobile.utils;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Centralised explicit wait helpers. Implicit waits should stay disabled (0)
 * when using these utilities to avoid unpredictable timing behaviour.
 */
public class WaitUtils {

    private final AppiumDriver driver;
    private final Duration timeout;
    private final Duration pollingInterval;
    private final WebDriverWait wait;

    public WaitUtils(AppiumDriver driver) {
        this(driver,
                Duration.ofSeconds(ConfigManager.getInt(FrameworkConstants.EXPLICIT_WAIT,
                        FrameworkConstants.DEFAULT_EXPLICIT_WAIT_SECONDS)),
                Duration.ofMillis(ConfigManager.getInt(FrameworkConstants.POLLING_INTERVAL,
                        FrameworkConstants.DEFAULT_POLLING_INTERVAL_MILLIS)));
    }

    public WaitUtils(AppiumDriver driver, Duration timeout, Duration pollingInterval) {
        this.driver = driver;
        this.timeout = timeout;
        this.pollingInterval = pollingInterval;
        WebDriverWait webDriverWait = new WebDriverWait(driver, timeout);
        webDriverWait.pollingEvery(pollingInterval);
        webDriverWait.ignoring(NoSuchElementException.class);
        webDriverWait.ignoring(StaleElementReferenceException.class);
        webDriverWait.ignoring(ElementNotInteractableException.class);
        this.wait = webDriverWait;
    }

    public WebElement untilVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement untilVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement untilClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement untilClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public WebElement untilPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public List<WebElement> untilAllPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    public boolean untilInvisible(By locator) {
        return Boolean.TRUE.equals(wait.until(ExpectedConditions.invisibilityOfElementLocated(locator)));
    }

    public boolean untilText(By locator, String expectedText) {
        return Boolean.TRUE.equals(wait.until(ExpectedConditions.textToBe(locator, expectedText)));
    }

    public boolean untilContainsText(By locator, String text) {
        return Boolean.TRUE.equals(wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text)));
    }

    public boolean untilCountGreaterThan(By locator, int count) {
        return wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, count)) != null;
    }

    public boolean untilAnyPresent(By... locators) {
        return Boolean.TRUE.equals(wait.until(webDriver -> {
            for (By locator : locators) {
                if (!webDriver.findElements(locator).isEmpty()) {
                    return true;
                }
            }
            return false;
        }));
    }

    public boolean isAnyPresent(By... locators) {
        try {
            return untilAnyPresent(locators);
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean isPresent(By locator) {
        try {
            untilPresent(locator);
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean isVisible(By locator) {
        try {
            untilVisible(locator);
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    /**
     * Immediate visibility probe — never waits. Returns {@code false} when the
     * element is missing, stale or not displayed, which makes it safe to use for
     * branching without paying the explicit-wait timeout.
     */
    public boolean isVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean isAnyVisible(WebElement... elements) {
        for (WebElement element : elements) {
            if (isVisible(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bounded wait used to synchronise on screen transitions. Unlike
     * {@link #isVisible(WebElement)} this blocks until one of the elements is
     * displayed or the timeout elapses.
     */
    public boolean isAnyVisibleWithin(Duration within, WebElement... elements) {
        WebDriverWait boundedWait = new WebDriverWait(driver, within);
        boundedWait.pollingEvery(pollingInterval);
        boundedWait.ignoring(NoSuchElementException.class);
        boundedWait.ignoring(StaleElementReferenceException.class);
        try {
            return Boolean.TRUE.equals(boundedWait.until(ignored -> isAnyVisible(elements)));
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean isAbsent(By locator) {
        return driver.findElements(locator).isEmpty();
    }

    /**
     * Waits a short while for the element to be visible. Used by the actions
     * that must not block the run for the full explicit timeout.
     */
    public WebElement untilVisibleWithin(Duration within, WebElement element) {
        try {
            return new WebDriverWait(driver, within)
                    .pollingEvery(pollingInterval)
                    .ignoring(StaleElementReferenceException.class)
                    .until(ExpectedConditions.visibilityOf(element));
        } catch (org.openqa.selenium.TimeoutException e) {
            return element;
        }
    }

    /** Waits a short while for the element to be clickable. */
    public WebElement untilClickableWithin(Duration within, WebElement element) {
        try {
            return new WebDriverWait(driver, within)
                    .pollingEvery(pollingInterval)
                    .ignoring(StaleElementReferenceException.class)
                    .until(ExpectedConditions.elementToBeClickable(element));
        } catch (org.openqa.selenium.TimeoutException e) {
            return element;
        }
    }

    public void waitForAbsence(By locator) {
        untilInvisible(locator);
    }

    public Duration timeout() {
        return timeout;
    }
}
