package com.enterprise.mobile.pages.android;

import com.enterprise.mobile.pages.BasePage;
import com.enterprise.mobile.pages.locators.android.MeeshoGalleryPageLocators;
import com.enterprise.mobile.utils.GestureUtils;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

/**
 * Full screen product image viewer (FullScreenViewPagerActivity).
 */
public class MeeshoGalleryPage extends BasePage {

    private static final Duration SWIPE_DURATION = Duration.ofMillis(350);

    private final MeeshoGalleryPageLocators locators;

    public MeeshoGalleryPage(AppiumDriver driver) {
        super(driver);
        locators = new MeeshoGalleryPageLocators();
        PageFactory.initElements(decorator, locators);
    }

    @Override
    public boolean isLoaded() {
        boolean loaded = awaitAnyDisplayed(20, locators.fullScreenImage);
        dumpPageSource("fullscreen-gallery");
        return loaded;
    }

    /** Quick check (without waiting) whether the gallery is the screen on top. */
    public boolean isOnGalleryScreen() {
        return !driver.findElements(
                io.appium.java_client.AppiumBy.id("com.meesho.supply:id/full_screen_image_view")).isEmpty();
    }

    /** True when the gallery shows more than one picture. */
    public boolean hasMultipleImages() {
        return !driver.findElements(
                io.appium.java_client.AppiumBy.id("com.meesho.supply:id/indicator")).isEmpty()
                || !driver.findElements(
                io.appium.java_client.AppiumBy.id("com.meesho.supply:id/img_right_chevron")).isEmpty();
    }

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

    /** Drags the image to the right (previous image) the given number of times. */
    public MeeshoGalleryPage swipeRight(int times) {
        for (int i = 0; i < times; i++) {
            horizontalSwipe(true);
        }
        return this;
    }

    /** Drags the image to the left (next image) the given number of times. */
    public MeeshoGalleryPage swipeLeft(int times) {
        for (int i = 0; i < times; i++) {
            horizontalSwipe(false);
        }
        return this;
    }

    /**
     * Scrolls the images gently: the drag starts on the centred image and
     * covers a short distance, so the pager simply moves to the next picture.
     */
    private void horizontalSwipe(boolean toRight) {
        Dimension size = driver.manage().window().getSize();
        int midY = size.getHeight() / 2;
        int distance = (int) (size.getWidth() * 0.35);
        int center = size.getWidth() / 2;
        Point start = new Point(toRight ? center + distance / 2 : center - distance / 2, midY);
        Point end = new Point(toRight ? center - distance / 2 : center + distance / 2, midY);
        GestureUtils.swipe(driver, start, end, SWIPE_DURATION);
        log.debug("Gallery swipe {}", toRight ? "right" : "left");
    }

    public MeeshoProductPage exit() {
        driver.navigate().back();
        return new MeeshoProductPage(driver);
    }
}
