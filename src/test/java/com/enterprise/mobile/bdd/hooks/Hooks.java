package com.enterprise.mobile.bdd.hooks;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.driver.AppiumServerManager;
import com.enterprise.mobile.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber hooks: start/stop the Appium server, record a screen video for
 * every scenario and split screenshots and videos into <code>target/screenshots/&lt;outcome&gt;</code>
 * and <code>target/videos/&lt;outcome&gt;</code> where <code>&lt;outcome&gt;</code>
 * is <code>pass</code> or <code>fail</code>.
 */
public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);

    private final com.enterprise.mobile.bdd.context.TestContext context;

    public Hooks(com.enterprise.mobile.bdd.context.TestContext context) {
        this.context = context;
    }

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        LOG.info("=== Scenario: {} ===", scenario.getName());
        com.enterprise.mobile.bdd.context.VerificationTracker.reset();
        AppiumServerManager.start();
        context.initDriver();
        if (ScreenshotUtils.isVideoRecordingEnabled() && context.getDriver() != null) {
            ScreenshotUtils.startRecording(context.getDriver());
        }
        if (context.getDevice() != null) {
            Allure.addAttachment("Device", "text/plain", context.getDevice().displayName());
        }
    }

    @Before(order = 1)
    public void ensureApplicationInForeground() {
        io.appium.java_client.AppiumDriver driver = context.getDriver();
        String packageName = ConfigManager.get("app.package", "");
        if (driver == null || packageName.isBlank() || packageName.startsWith("com.android.")) {
            return;
        }
        dismissThirdPartyLoginActivities(driver, packageName);
        if (!isInForeground(driver, packageName)) {
            LOG.info("Bringing {} to the foreground without restarting it", packageName);
            driver.executeScript("mobile: activateApp", java.util.Map.of("appId", packageName));
        }
        dismissMeeshoPopUps(driver);
    }

    /**
     * The language chooser and the gender chooser can pop up over the app and
     * block every following step, so they are answered (English / Male) as soon
     * as they appear.
     */
    private void dismissMeeshoPopUps(io.appium.java_client.AppiumDriver driver) {
        try {
            com.enterprise.mobile.pages.android.MeeshoOnboardingDialogs dialogs =
                    new com.enterprise.mobile.pages.android.MeeshoOnboardingDialogs(driver);
            for (int attempt = 0; attempt < 3; attempt++) {
                if (!dialogs.dismissVisibleDialogs()) {
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("Could not handle the Meesho pop-ups: {}", e.getMessage());
        }
    }

    private boolean isInForeground(io.appium.java_client.AppiumDriver driver, String packageName) {
        if (driver instanceof io.appium.java_client.android.AndroidDriver android) {
            return packageName.equals(android.getCurrentPackage());
        }
        return true;
    }

    /**
     * Sign-up flows can leave Truecaller or Google credential activities on top
     * of the app, which makes every subsequent lookup wait for its full
     * timeout. Press Back until those third-party screens are gone.
     */
    private void dismissThirdPartyLoginActivities(io.appium.java_client.AppiumDriver driver,
                                                  String packageName) {
        for (int attempt = 0; attempt < 3; attempt++) {
            String current = driver instanceof io.appium.java_client.android.AndroidDriver android
                    ? android.getCurrentPackage() : null;
            boolean stray = current != null && !current.equals(packageName)
                    && (current.startsWith("com.truecaller") || current.startsWith("com.google.android.gms"));
            if (!stray) {
                return;
            }
            LOG.info("Third-party login activity '{}' is in front; pressing Back", current);
            driver.navigate().back();
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @io.cucumber.java.AfterStep
    public void afterStep(Scenario scenario) {
        // Always show where the execution stands and what is still open.
        LOG.info("PROGRESS {}",
                com.enterprise.mobile.bdd.context.VerificationTracker.snapshot());
    }

    @After(order = 0)
    public void afterScenario(Scenario scenario) {        try {
            boolean failed = scenario.isFailed();
            String outcome = failed ? ScreenshotUtils.OUTCOME_FAIL : ScreenshotUtils.OUTCOME_PASS;

            if (context.getDriver() != null) {
                boolean captureScreenshot = failed
                        ? ConfigManager.getBoolean(FrameworkConstants.SCREENSHOT_ON_FAILURE, true)
                        : ConfigManager.getBoolean(FrameworkConstants.SCREENSHOT_ON_SUCCESS, false);

                if (captureScreenshot) {
                    String name = (failed ? "Failure screenshot" : "Success screenshot") + " - " + scenario.getName();
                    ScreenshotUtils.attach(context.getDriver(), name, outcome);
                }
                if (failed) {
                    String sourceName = "Page source - " + scenario.getName();
                    ScreenshotUtils.attachPageSource(context.getDriver(), sourceName, outcome);
                }
                if (ScreenshotUtils.isVideoRecordingEnabled()) {
                    String videoName = (failed ? "Failure video" : "Success video") + " - " + scenario.getName();
                    ScreenshotUtils.stopRecording(context.getDriver(), videoName, outcome);
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("Unable to capture scenario evidence: {}", e.getMessage());
        } finally {
            context.quitDriver();
        }
    }
}
