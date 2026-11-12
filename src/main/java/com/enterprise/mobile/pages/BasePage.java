package com.enterprise.mobile.pages;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.driver.DriverManager;
import com.enterprise.mobile.utils.GestureUtils;
import com.enterprise.mobile.utils.WaitUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Common behaviour for every screen. Pages are composed of the Page Object Model
 * (behaviour) and Page Factory locators (declarative fields) — but in this project
 * the locators live in dedicated <code>pages.locators.*Locators</code> classes
 * owned one-per-screen. Pages keep only public methods; the locator classes are
 * plain data classes initialised by {@code PageFactory.initElements} here.
 */
public abstract class BasePage {

    /** Short wait used for the actions: a missing element must not block the run. */
    protected static final Duration FAST_WAIT = Duration.ofSeconds(4);

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final AppiumDriver driver;
    protected final WaitUtils wait;
    protected final AppiumFieldDecorator decorator;

    protected BasePage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.decorator = new AppiumFieldDecorator(driver, Duration.ZERO);
        PageFactory.initElements(decorator, this);
    }

    protected BasePage() {
        this(DriverManager.getDriver());
    }

    /**
     * @return {@code true} when the screen is displayed and ready for
     * interaction. Useful as an explicit synchronisation point.
     */
    public abstract boolean isLoaded();

    // ------------------------------------------------------------------
    // Element helpers
    // ------------------------------------------------------------------

    protected WebElement visible(WebElement element) {
        return wait.untilVisible(element);
    }

    protected boolean isDisplayed(WebElement element) {
        return wait.isVisible(element);
    }

    protected boolean isPresent(WebElement element) {
        return wait.isVisible(element);
    }

    protected boolean isAnyDisplayed(WebElement... elements) {
        return wait.isAnyVisible(elements);
    }

    /**
     * Bounded synchronisation for screen transitions — waits until one of the
     * elements becomes visible (or the timeout elapses). Use inside
     * {@link #isLoaded()} implementations.
     */
    protected boolean awaitAnyDisplayed(int seconds, WebElement... elements) {
        return wait.isAnyVisibleWithin(Duration.ofSeconds(seconds), elements);
    }

    /** Short deterministic pause used between navigation steps. */
    protected static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    /**
     * Looks for an element and, when it is not on screen, scrolls the page up
     * and down to find it.
     *
     * @return the visible element, or {@code null} when it cannot be found
     */
    protected WebElement findByScrolling(By locator) {
        for (int attempt = 0; attempt < 3; attempt++) {
            WebElement found = firstVisible(locator);
            if (found != null) {
                return found;
            }
            swipeUp();
        }
        for (int attempt = 0; attempt < 3; attempt++) {
            WebElement found = firstVisible(locator);
            if (found != null) {
                return found;
            }
            swipeDown();
        }
        return null;
    }

    /** True when the element can be found on screen, scrolling up and down. */
    protected boolean isVisibleAfterScrolling(By locator) {
        return findByScrolling(locator) != null;
    }

    /**
     * Scrolls the page up and down to find the element and clicks it. When the
     * element still refuses the normal click, a forced tap on its centre is
     * used instead - the flow never restarts from the first step.
     */
    protected void clickByScrolling(By locator) {
        WebElement element = findByScrolling(locator);
        if (element == null) {
            throw new IllegalStateException(
                    "The element " + locator + " is not on screen and could not be found");
        }
        clickForcefully(element);
    }

    /** Clicks the element and falls back to a forced tap on its centre. */
    protected void clickForcefully(WebElement element) {
        try {
            wait.untilClickableWithin(FAST_WAIT, element).click();
            return;
        } catch (RuntimeException e) {
            log.warn("The element was not clickable, forcing the click: {}", e.getMessage());
        }
        Rectangle box = element.getRect();
        GestureUtils.tap(driver, box.getX() + box.getWidth() / 2,
                box.getY() + box.getHeight() / 2);
    }

    private WebElement firstVisible(By locator) {
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

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    protected void click(WebElement element) {
        clickForcefully(element);
    }

    protected void tap(WebElement element) {
        clickForcefully(element);
    }

    protected void type(WebElement element, String text) {
        WebElement resolved = wait.untilVisibleWithin(FAST_WAIT, element);
        resolved.clear();
        resolved.sendKeys(text);
        hideKeyboard();
    }

    protected String textOf(WebElement element) {
        return wait.untilVisibleWithin(FAST_WAIT, element).getText();
    }

    protected void hideKeyboard() {
        try {
            if (driver instanceof HidesKeyboard hidesKeyboard) {
                hidesKeyboard.hideKeyboard();
            }
        } catch (RuntimeException ignored) {
            // Keyboard may not be visible; nothing to do.
        }
    }

    public void swipeUp() {
        GestureUtils.swipeUp(driver);
    }

    public void swipeDown() {
        GestureUtils.swipeDown(driver);
    }

    public String getPageSource() {
        return driver.getPageSource();
    }
}
