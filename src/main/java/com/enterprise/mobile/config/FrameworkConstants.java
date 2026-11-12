package com.enterprise.mobile.config;

/**
 * Central registry of configuration keys and framework defaults. Keeping the
 * keys in one place avoids magic strings and makes the configuration surface
 * discoverable.
 */
public final class FrameworkConstants {

    private FrameworkConstants() {
    }

    // ------------------------------------------------------------------
    // General / platform
    // ------------------------------------------------------------------
    public static final String ENV = "env";
    public static final String PLATFORM = "platform";

    // ------------------------------------------------------------------
    // Appium server
    // ------------------------------------------------------------------
    public static final String APPIUM_SERVER_MANAGED = "appium.server.managed";
    public static final String APPIUM_SERVER_URL = "appium.server.url";
    public static final String APPIUM_SERVER_HOST = "appium.server.host";
    public static final String APPIUM_SERVER_PORT = "appium.server.port";
    public static final String APPIUM_SERVER_BASE_PATH = "appium.server.basePath";
    public static final String APPIUM_SERVER_LOG_LEVEL = "appium.server.logLevel";
    public static final String APPIUM_SERVER_APPIUM_JS = "appium.server.appium.js";

    // ------------------------------------------------------------------
    // Cloud providers (BrowserStack / Sauce Labs compatible)
    // ------------------------------------------------------------------
    public static final String CLOUD_ENABLED = "cloud.enabled";
    public static final String CLOUD_PROVIDER = "cloud.provider";
    public static final String CLOUD_URL = "cloud.url";
    public static final String CLOUD_USER = "cloud.user";
    public static final String CLOUD_KEY = "cloud.key";

    // ------------------------------------------------------------------
    // Device selection
    // ------------------------------------------------------------------
    public static final String DEVICE_NAME = "device.name";
    public static final String DEVICE_UDID = "device.udid";
    public static final String DEVICE_PLATFORM_VERSION = "device.platformVersion";
    public static final String DEVICE_AUTOMATION_NAME = "device.automationName";
    public static final String DEVICE_REGISTRY = "device.registry";
    public static final String DEVICE_REGISTRY_FILE = "device.registry.file";

    // ------------------------------------------------------------------
    // Application under test
    // ------------------------------------------------------------------
    public static final String APP_PATH = "app.path";
    public static final String APP_PACKAGE = "app.package";
    public static final String APP_ACTIVITY = "app.activity";
    public static final String APP_BUNDLE_ID = "app.bundleId";
    public static final String APP_NO_RESET = "app.noReset";
    public static final String APP_FULL_RESET = "app.fullReset";
    public static final String APP_FORCE_LAUNCH = "app.forceLaunch";
    public static final String APP_TERMINATE_ON_QUIT = "app.terminateOnQuit";
    public static final String APP_SKIP_DEVICE_INIT = "app.skipDeviceInitialization";
    public static final String APP_AUTO_GRANT_PERMISSIONS = "app.autoGrantPermissions";

    // ------------------------------------------------------------------
    // Timeouts (seconds unless stated otherwise)
    // ------------------------------------------------------------------
    public static final String IMPLICIT_WAIT = "implicit.wait";
    public static final String EXPLICIT_WAIT = "explicit.wait";
    public static final String POLLING_INTERVAL = "polling.interval";
    public static final String NEW_COMMAND_TIMEOUT = "new.command.timeout";
    public static final String ADB_EXEC_TIMEOUT = "adb.exec.timeout";
    public static final String WDA_LAUNCH_TIMEOUT = "wda.launch.timeout";
    public static final String WDA_CONNECTION_TIMEOUT = "wda.connection.timeout";

    // ------------------------------------------------------------------
    // Framework behaviour
    // ------------------------------------------------------------------
    public static final String RETRY_COUNT = "retry.count";
    public static final String SCREENSHOT_ON_FAILURE = "screenshot.on.failure";
    public static final String SCREENSHOT_ON_SUCCESS = "screenshot.on.success";
    public static final String VIDEO_RECORDING_ENABLED = "video.recording.enabled";
    public static final String CAPABILITY_PREFIX = "capability.";
    public static final String ADB_PATH = "adb.path";

    // ------------------------------------------------------------------
    // Defaults
    // ------------------------------------------------------------------
    public static final String DEFAULT_PLATFORM = "android";
    public static final String DEFAULT_APPIUM_HOST = "127.0.0.1";
    public static final int DEFAULT_APPIUM_PORT = 4723;
    public static final String DEFAULT_APPIUM_BASE_PATH = "/";
    public static final int DEFAULT_EXPLICIT_WAIT_SECONDS = 20;
    public static final int DEFAULT_POLLING_INTERVAL_MILLIS = 500;
    public static final int DEFAULT_NEW_COMMAND_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_RETRY_COUNT = 1;
    public static final String DEVICE_REGISTRY_RESOURCE = "devices/devices.json";
}
