package com.enterprise.mobile.driver;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.exceptions.FrameworkException;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Optionally starts and stops a local Appium server for the duration of a test
 * suite. When {@code appium.server.managed=false} the framework connects to the
 * URL provided by {@code appium.server.url} (local, Docker or a cloud grid).
 */
public final class AppiumServerManager {

    private static final Logger LOG = LoggerFactory.getLogger(AppiumServerManager.class);
    private static final Object LOCK = new Object();

    private static volatile AppiumDriverLocalService service;

    private AppiumServerManager() {
    }

    public static void start() {
        synchronized (LOCK) {
            if (!ConfigManager.getBoolean(FrameworkConstants.APPIUM_SERVER_MANAGED, false)) {
                LOG.info("Appium server lifecycle is not managed by the framework; using {}",
                        ConfigManager.get(FrameworkConstants.APPIUM_SERVER_URL, defaultServerUrl()));
                return;
            }
            if (service != null && service.isRunning()) {
                LOG.info("Appium server already running at {}", service.getUrl());
                return;
            }

            String host = ConfigManager.get(FrameworkConstants.APPIUM_SERVER_HOST,
                    FrameworkConstants.DEFAULT_APPIUM_HOST);
            int port = ConfigManager.getInt(FrameworkConstants.APPIUM_SERVER_PORT,
                    FrameworkConstants.DEFAULT_APPIUM_PORT);
            String basePath = ConfigManager.get(FrameworkConstants.APPIUM_SERVER_BASE_PATH,
                    FrameworkConstants.DEFAULT_APPIUM_BASE_PATH);
            String logLevel = ConfigManager.get(FrameworkConstants.APPIUM_SERVER_LOG_LEVEL, "info");

            AppiumServiceBuilder builder = new AppiumServiceBuilder()
                    .withIPAddress(host)
                    .usingPort(port)
                    .withArgument(GeneralServerFlag.BASEPATH, basePath)
                    .withArgument(GeneralServerFlag.LOG_LEVEL, logLevel)
                    .withArgument(GeneralServerFlag.RELAXED_SECURITY);

            String appiumJs = ConfigManager.get(FrameworkConstants.APPIUM_SERVER_APPIUM_JS);
            if (appiumJs != null && !appiumJs.isBlank()) {
                File appiumJsFile = new File(appiumJs);
                if (!appiumJsFile.exists()) {
                    throw new FrameworkException("Configured appium JS entry point does not exist: " + appiumJsFile);
                }
                builder.withAppiumJS(appiumJsFile);
            }

            LOG.info("Starting managed Appium server on {}:{} (basePath={})", host, port, basePath);
            service = AppiumDriverLocalService.buildService(builder);
            service.start();
            if (!service.isRunning()) {
                throw new FrameworkException("Managed Appium server failed to start");
            }
            LOG.info("Managed Appium server is ready at {}", service.getUrl());
        }
    }

    public static void stop() {
        synchronized (LOCK) {
            if (service != null && service.isRunning()) {
                LOG.info("Stopping managed Appium server");
                service.stop();
            }
            service = null;
        }
    }

    public static boolean isRunning() {
        return service != null && service.isRunning();
    }

    public static URL getServerUrl() {
        if (ConfigManager.getBoolean(FrameworkConstants.CLOUD_ENABLED, false)) {
            return toUrl(ConfigManager.getRequired(FrameworkConstants.CLOUD_URL));
        }
        if (service != null && service.isRunning()) {
            return service.getUrl();
        }
        return toUrl(ConfigManager.get(FrameworkConstants.APPIUM_SERVER_URL, defaultServerUrl()));
    }

    private static String defaultServerUrl() {
        String host = ConfigManager.get(FrameworkConstants.APPIUM_SERVER_HOST,
                FrameworkConstants.DEFAULT_APPIUM_HOST);
        int port = ConfigManager.getInt(FrameworkConstants.APPIUM_SERVER_PORT,
                FrameworkConstants.DEFAULT_APPIUM_PORT);
        return "http://%s:%d".formatted(host, port);
    }

    private static URL toUrl(String rawUrl) {
        try {
            return URI.create(rawUrl.trim()).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new FrameworkException("Invalid Appium server URL: " + rawUrl, e);
        }
    }
}
