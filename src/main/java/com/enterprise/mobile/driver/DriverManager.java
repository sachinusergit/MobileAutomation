package com.enterprise.mobile.driver;

import com.enterprise.mobile.exceptions.FrameworkException;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the {@link AppiumDriver} instance for the current thread. Thread local
 * storage enables safe parallel execution in TestNG.
 */
public final class DriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(DriverManager.class);

    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<Device> DEVICE = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void initDriver() {
        initDriver(DeviceAllocator.acquire());
    }

    public static void initDriver(Device device) {
        if (DRIVER.get() != null) {
            LOG.warn("A driver already exists for thread '{}'; quitting it before creating a new one",
                    Thread.currentThread().getName());
            quitDriver();
        }
        try {
            AppiumDriver driver = DriverFactory.create(device);
            DRIVER.set(driver);
            DEVICE.set(device);
        } catch (RuntimeException e) {
            DeviceAllocator.release(device);
            throw new FrameworkException("Unable to create driver for " + device.displayName(), e);
        }
    }

    public static AppiumDriver getDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException("No driver bound to thread '%s'. Was initDriver() called?"
                    .formatted(Thread.currentThread().getName()));
        }
        return driver;
    }

    public static AppiumDriver getDriverOrNull() {
        return DRIVER.get();
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static Device getDevice() {
        Device device = DEVICE.get();
        if (device == null) {
            throw new FrameworkException("No device bound to thread '" + Thread.currentThread().getName() + "'");
        }
        return device;
    }

    public static Platform getPlatform() {
        return getDevice().platformEnum();
    }

    public static void quitDriver() {
        AppiumDriver driver = DRIVER.get();
        Device device = DEVICE.get();
        try {
            if (driver != null) {
                LOG.info("Closing driver session {}", driver.getSessionId());
                driver.quit();
            }
        } catch (RuntimeException e) {
            LOG.warn("Error while quitting driver: {}", e.getMessage());
        } finally {
            DRIVER.remove();
            DEVICE.remove();
            DeviceAllocator.release(device);
        }
    }
}
