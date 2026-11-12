package com.enterprise.mobile.driver;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.exceptions.FrameworkException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds fully configured {@link AppiumDriver} instances for Android and iOS.
 * All capabilities are driven by configuration so the same test code can run
 * against local devices, emulators or a cloud device farm.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static AppiumDriver create(Device device) {
        Platform platform = device.platformEnum();
        URL serverUrl = AppiumServerManager.getServerUrl();
        LOG.info("Creating {} driver for {} at {}", platform.value().toUpperCase(), device.displayName(), serverUrl);

        prepareDevice(device);

        AppiumDriver driver = createSession(platform, serverUrl, device);
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(
                    ConfigManager.getInt(FrameworkConstants.IMPLICIT_WAIT, 0)));
            LOG.info("{} driver session established: {}", platform.value().toUpperCase(), driver.getSessionId());
            return driver;
        } catch (RuntimeException e) {
            // The UiAutomator2 instrumentation can die between the session
            // creation and the first command (for example when the phone
            // screen was locked). A fresh session recovers the run.
            LOG.warn("The new session could not be used ({}); creating a new one", e.getMessage());
            try {
                driver.quit();
            } catch (RuntimeException ignored) {
                // The session is already gone.
            }
            AppiumDriver retried = createSession(platform, serverUrl, device);
            retried.manage().timeouts().implicitlyWait(Duration.ofSeconds(
                    ConfigManager.getInt(FrameworkConstants.IMPLICIT_WAIT, 0)));
            LOG.info("{} driver session re-established: {}", platform.value().toUpperCase(),
                    retried.getSessionId());
            return retried;
        }
    }

    private static AppiumDriver createSession(Platform platform, URL serverUrl, Device device) {
        return switch (platform) {
            case ANDROID -> new AndroidDriver(serverUrl, androidOptions(device));
            case IOS -> new IOSDriver(serverUrl, iosOptions(device));
        };
    }

    /**
     * Makes sure the phone is reachable before the session is created: a
     * wireless device that went offline is reconnected and its screen is kept
     * on so it does not drop off during the run.
     */
    private static void prepareDevice(Device device) {
        String udid = firstNonBlank(device.getUdid(), ConfigManager.get(FrameworkConstants.DEVICE_UDID));
        if (udid == null || udid.isBlank() || device.platformEnum() != Platform.ANDROID) {
            return;
        }
        try {
            if (!com.enterprise.mobile.utils.AdbUtils.waitForDeviceOnline(udid, 30)) {
                LOG.warn("The device {} did not come online", udid);
                return;
            }
            com.enterprise.mobile.utils.AdbUtils.keepScreenOn(udid);
        } catch (RuntimeException e) {
            LOG.warn("Could not prepare the device {}: {}", udid, e.getMessage());
        }
    }

    private static UiAutomator2Options androidOptions(Device device) {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName(firstNonBlank(device.getAutomationName(),
                ConfigManager.get(FrameworkConstants.DEVICE_AUTOMATION_NAME, "UiAutomator2")));
        options.setDeviceName(firstNonBlank(device.getName(),
                ConfigManager.get(FrameworkConstants.DEVICE_NAME, "AndroidDevice")));
        setUdidAndVersion(options, device);

        options.setNewCommandTimeout(Duration.ofSeconds(
                ConfigManager.getInt(FrameworkConstants.NEW_COMMAND_TIMEOUT,
                        FrameworkConstants.DEFAULT_NEW_COMMAND_TIMEOUT_SECONDS)));
        int adbTimeout = ConfigManager.getInt(FrameworkConstants.ADB_EXEC_TIMEOUT, 60);
        options.setAdbExecTimeout(Duration.ofSeconds(adbTimeout));

        applyAndroidAppOptions(options);
        applyResetOptions(options);
        if (ConfigManager.getBoolean(FrameworkConstants.APP_AUTO_GRANT_PERMISSIONS, false)) {
            options.setAutoGrantPermissions(true);
        }

        applyCustomCapabilities(options, device);
        return options;
    }

    private static XCUITestOptions iosOptions(Device device) {
        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName("iOS");
        options.setAutomationName(firstNonBlank(device.getAutomationName(),
                ConfigManager.get(FrameworkConstants.DEVICE_AUTOMATION_NAME, "XCUITest")));
        options.setDeviceName(firstNonBlank(device.getName(),
                ConfigManager.get(FrameworkConstants.DEVICE_NAME, "iPhone")));
        setUdidAndVersion(options, device);

        options.setNewCommandTimeout(Duration.ofSeconds(
                ConfigManager.getInt(FrameworkConstants.NEW_COMMAND_TIMEOUT,
                        FrameworkConstants.DEFAULT_NEW_COMMAND_TIMEOUT_SECONDS)));
        int wdaLaunchTimeout = ConfigManager.getInt(FrameworkConstants.WDA_LAUNCH_TIMEOUT, 120);
        int wdaConnectionTimeout = ConfigManager.getInt(FrameworkConstants.WDA_CONNECTION_TIMEOUT, 120);
        options.setWdaLaunchTimeout(Duration.ofSeconds(wdaLaunchTimeout));
        options.setWdaConnectionTimeout(Duration.ofSeconds(wdaConnectionTimeout));

        applyIosAppOptions(options);
        applyResetOptions(options);

        applyCustomCapabilities(options, device);
        return options;
    }

    private static void setUdidAndVersion(MutableCapabilities options, Device device) {
        String udid = firstNonBlank(device.getUdid(), ConfigManager.get(FrameworkConstants.DEVICE_UDID));
        if (udid != null && !udid.isBlank()) {
            options.setCapability("udid", udid);
        }
        String platformVersion = firstNonBlank(device.getPlatformVersion(),
                ConfigManager.get(FrameworkConstants.DEVICE_PLATFORM_VERSION));
        if (platformVersion != null && !platformVersion.isBlank()) {
            options.setCapability("platformVersion", platformVersion);
        }
    }

    private static void applyAndroidAppOptions(UiAutomator2Options options) {
        String appPath = resolveAppPath(ConfigManager.get(FrameworkConstants.APP_PATH));
        if (appPath != null) {
            options.setApp(appPath);
        }
        String appPackage = ConfigManager.get(FrameworkConstants.APP_PACKAGE);
        if (appPackage != null && !appPackage.isBlank()) {
            options.setAppPackage(appPackage.trim());
        }
        String appActivity = ConfigManager.get(FrameworkConstants.APP_ACTIVITY);
        if (appActivity != null && !appActivity.isBlank()) {
            options.setAppActivity(appActivity.trim());
        }
        options.setCapability("forceAppLaunch",
                ConfigManager.getBoolean(FrameworkConstants.APP_FORCE_LAUNCH, false));
        options.setCapability("shouldTerminateApp",
                ConfigManager.getBoolean(FrameworkConstants.APP_TERMINATE_ON_QUIT, false));
        options.setCapability("skipDeviceInitialization",
                ConfigManager.getBoolean(FrameworkConstants.APP_SKIP_DEVICE_INIT, true));
    }

    private static void applyIosAppOptions(XCUITestOptions options) {
        String appPath = resolveAppPath(ConfigManager.get(FrameworkConstants.APP_PATH));
        if (appPath != null) {
            options.setApp(appPath);
        }
        String bundleId = ConfigManager.get(FrameworkConstants.APP_BUNDLE_ID);
        if (bundleId != null && !bundleId.isBlank()) {
            options.setBundleId(bundleId.trim());
        }
    }

    private static void applyResetOptions(MutableCapabilities options) {
        options.setCapability("noReset",
                ConfigManager.getBoolean(FrameworkConstants.APP_NO_RESET, true));
        options.setCapability("fullReset",
                ConfigManager.getBoolean(FrameworkConstants.APP_FULL_RESET, false));
    }

    private static void applyCustomCapabilities(MutableCapabilities options, Device device) {
        ConfigManager.getPropertiesWithPrefix(FrameworkConstants.CAPABILITY_PREFIX)
                .forEach((name, value) -> options.setCapability(name, parseValue(value)));
        device.getCapabilities().forEach(options::setCapability);
        applyCloudAuthentication(options);
    }

    private static void applyCloudAuthentication(MutableCapabilities options) {
        if (!ConfigManager.getBoolean(FrameworkConstants.CLOUD_ENABLED, false)) {
            return;
        }
        String provider = ConfigManager.get(FrameworkConstants.CLOUD_PROVIDER, "browserstack");
        String user = ConfigManager.get(FrameworkConstants.CLOUD_USER);
        String key = ConfigManager.get(FrameworkConstants.CLOUD_KEY);

        Map<String, Object> vendorOptions = new LinkedHashMap<>();
        if (user != null && !user.isBlank()) {
            vendorOptions.put("userName", user.trim());
        }
        if (key != null && !key.isBlank()) {
            vendorOptions.put("accessKey", key.trim());
        }
        if (vendorOptions.isEmpty()) {
            LOG.warn("Cloud execution enabled but no credentials were supplied; "
                    + "set cloud.user / cloud.key or CLOUD_USER / CLOUD_KEY");
            return;
        }

        switch (provider.toLowerCase()) {
            case "browserstack" -> options.setCapability("bstack:options", vendorOptions);
            case "sauce", "saucelabs" -> options.setCapability("sauce:options", vendorOptions);
            default -> LOG.warn("Unsupported cloud provider '{}'; cloud capabilities were not applied", provider);
        }
    }

    private static String resolveAppPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return null;
        }
        Path path = Paths.get(rawPath.trim());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        if (!Files.exists(path)) {
            throw new FrameworkException("Application file not found: " + path.toAbsolutePath());
        }
        return path.toAbsolutePath().toString();
    }

    private static Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Boolean.parseBoolean(trimmed);
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
            return trimmed;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
