package com.enterprise.mobile.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;

/**
 * Cross-platform W3C gesture helpers built on Selenium's low level actions
 * API, which works for both Android and iOS.
 */
public final class GestureUtils {

    private static final Duration DEFAULT_SWIPE_DURATION = Duration.ofMillis(600);
    private static final Duration LONG_PRESS_DURATION = Duration.ofSeconds(1);

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private GestureUtils() {
    }

    public static void swipeUp(AppiumDriver driver) {
        swipe(driver, Direction.UP, 0.75);
    }

    public static void swipeDown(AppiumDriver driver) {
        swipe(driver, Direction.DOWN, 0.75);
    }

    public static void swipeLeft(AppiumDriver driver) {
        swipe(driver, Direction.LEFT, 0.75);
    }

    public static void swipeRight(AppiumDriver driver) {
        swipe(driver, Direction.RIGHT, 0.75);
    }

    public static void swipe(AppiumDriver driver, Direction direction, double distanceRatio) {
        if (distanceRatio <= 0 || distanceRatio > 1) {
            throw new IllegalArgumentException("distanceRatio must be in the range (0, 1]");
        }
        Dimension size = driver.manage().window().getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int horizontalDistance = (int) (width * distanceRatio / 2);
        int verticalDistance = (int) (height * distanceRatio / 2);

        Point start;
        Point end;
        switch (direction) {
            case UP -> {
                start = new Point(centerX, centerY + verticalDistance);
                end = new Point(centerX, centerY - verticalDistance);
            }
            case DOWN -> {
                start = new Point(centerX, centerY - verticalDistance);
                end = new Point(centerX, centerY + verticalDistance);
            }
            case LEFT -> {
                start = new Point(centerX + horizontalDistance, centerY);
                end = new Point(centerX - horizontalDistance, centerY);
            }
            case RIGHT -> {
                start = new Point(centerX - horizontalDistance, centerY);
                end = new Point(centerX + horizontalDistance, centerY);
            }
            default -> throw new IllegalStateException("Unexpected direction: " + direction);
        }
        swipe(driver, start, end, DEFAULT_SWIPE_DURATION);
    }

    public static void swipe(AppiumDriver driver, Point start, Point end) {
        swipe(driver, start, end, DEFAULT_SWIPE_DURATION);
    }

    public static void swipe(AppiumDriver driver, Point start, Point end, Duration duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                        start.getX(), start.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(150)))
                .addAction(finger.createPointerMove(duration, PointerInput.Origin.viewport(),
                        end.getX(), end.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    public static void tap(AppiumDriver driver, WebElement element) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.fromElement(element), 0, 0))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(100)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    public static void tap(AppiumDriver driver, int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(100)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    public static void longPress(AppiumDriver driver, WebElement element) {
        longPress(driver, element, LONG_PRESS_DURATION);
    }

    public static void longPress(AppiumDriver driver, WebElement element, Duration duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.fromElement(element), 0, 0))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, duration))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    public static void dragAndDrop(AppiumDriver driver, WebElement source, WebElement target) {
        Point sourceCenter = centerOf(source);
        Point targetCenter = centerOf(target);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                        sourceCenter.getX(), sourceCenter.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(500)))
                .addAction(finger.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(),
                        targetCenter.getX(), targetCenter.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    private static Point centerOf(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        return new Point(location.getX() + size.getWidth() / 2, location.getY() + size.getHeight() / 2);
    }
}
