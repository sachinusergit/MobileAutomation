package com.enterprise.mobile.utils;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures screenshots and video and stores them under <code>target/screenshots</code>
 * or <code>target/videos</code>, split into a <code>pass</code> or <code>fail</code>
 * subdirectory so passing and failing cases can be reviewed separately.
 */
public final class ScreenshotUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    /** Directory base kept under target/ so it is ignored by git. */
    public static final String SCREENSHOT_DIR = Paths.get("target", "screenshots").toString();
    public static final String VIDEO_DIR = Paths.get("target", "videos").toString();

    public static final String OUTCOME_PASS = "pass";
    public static final String OUTCOME_FAIL = "fail";

    private ScreenshotUtils() {
    }

    // ------------------------------------------------------------------
    // Screenshots
    // ------------------------------------------------------------------

    public static byte[] capture(AppiumDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Saves a screenshot under {@code target/screenshots/<outcome>/} and attaches it to Allure.
     */
    public static Path save(AppiumDriver driver, String name, String outcome) {
        return save(driver, name, outcome, System.currentTimeMillis());
    }

    /** Same as {@link #save(AppiumDriver, String, String)} with an explicit timestamp suffix. */
    public static Path save(AppiumDriver driver, String name, String outcome, long timestamp) {
        try {
            Path dir = Paths.get(SCREENSHOT_DIR, outcome);
            Files.createDirectories(dir);
            String safeName = sanitise(name);
            Path destination = dir.resolve(
                    safeName + "-" + LocalDateTime.now().format(TIMESTAMP) + "-" + timestamp + ".png");
            Files.write(destination, capture(driver));
            LOG.info("Screenshot saved to {}", destination.toAbsolutePath());
            return destination;
        } catch (IOException e) {
            LOG.warn("Unable to save screenshot '{}': {}", name, e.getMessage());
            return null;
        }
    }

    public static void attach(AppiumDriver driver, String name) {
        try {
            byte[] screenshot = capture(driver);
            Allure.addAttachment(name, "image/png",
                    new ByteArrayInputStream(screenshot), "png");
        } catch (RuntimeException e) {
            LOG.warn("Unable to attach screenshot '{}': {}", name, e.getMessage());
        }
    }

    /**
     * Saves the screenshot under {@code target/screenshots/<outcome>/} and
     * attaches the same bytes to Allure.
     */
    public static void attach(AppiumDriver driver, String name, String outcome) {
        try {
            byte[] screenshot = capture(driver);
            save(driver, name, outcome);
            Allure.addAttachment(name + " (" + outcome + ")", "image/png",
                    new ByteArrayInputStream(screenshot), "png");
        } catch (RuntimeException e) {
            LOG.warn("Unable to capture/save screenshot '{}': {}", name, e.getMessage());
        }
    }

    public static void attachPageSource(AppiumDriver driver, String name) {
        attachPageSource(driver, name, OUTCOME_FAIL);
    }

    public static void attachPageSource(AppiumDriver driver, String name, String outcome) {
        try {
            String source = driver.getPageSource();
            byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
            Path dir = Paths.get(SCREENSHOT_DIR, outcome);
            Files.createDirectories(dir);
            String safeName = sanitise(name);
            Files.write(dir.resolve(safeName + "-" + LocalDateTime.now().format(TIMESTAMP) + ".xml"), bytes);
            Allure.addAttachment(name + " (" + outcome + ")", "text/xml",
                    new ByteArrayInputStream(bytes), "xml");
        } catch (RuntimeException | IOException e) {
            LOG.warn("Unable to attach page source '{}': {}", name, e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Video recording
    // ------------------------------------------------------------------

    public static void startRecording(AppiumDriver driver) {
        try {
            ((io.appium.java_client.screenrecording.CanRecordScreen) driver).startRecordingScreen();
            LOG.info("Screen video recording started");
        } catch (RuntimeException e) {
            LOG.warn("Screen recording is not supported by this Appium server/driver: {}",
                    e.getMessage());
        }
    }

    public static byte[] stopRecording(AppiumDriver driver) {
        try {
            String base64 = ((io.appium.java_client.screenrecording.CanRecordScreen) driver).stopRecordingScreen();
            return base64 == null || base64.isBlank() ? new byte[0] : java.util.Base64.getDecoder().decode(base64);
        } catch (RuntimeException e) {
            LOG.warn("Unable to stop screen recording: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Stops recording, decodes the video and saves it under
     * {@code target/videos/<outcome>/}. The same bytes are attached to Allure.
     */
    public static void stopRecording(AppiumDriver driver, String name, String outcome) {
        byte[] video = stopRecording(driver);
        if (video.length == 0) {
            LOG.warn("No video captured for '{}' - skipping save", name);
            return;
        }
        try {
            Path dir = Paths.get(VIDEO_DIR, outcome);
            Files.createDirectories(dir);
            String safeName = sanitise(name);
            Path destination = dir.resolve(
                    safeName + "-" + LocalDateTime.now().format(TIMESTAMP) + ".mp4");
            Files.write(destination, video);
            Allure.addAttachment(name + " video (" + outcome + ")", "video/mp4",
                    new ByteArrayInputStream(video), "mp4");
            LOG.info("Video saved to {}", destination.toAbsolutePath());
        } catch (IOException e) {
            LOG.warn("Unable to save video '{}': {}", name, e.getMessage());
        }
    }

    /** Convenience for hooks; module reads its own configuration. */
    public static boolean isVideoRecordingEnabled() {
        return ConfigManager.getBoolean(FrameworkConstants.VIDEO_RECORDING_ENABLED, true);
    }

    private static String sanitise(String value) {
        if (value == null || value.isBlank()) {
            return "screenshot";
        }
        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
