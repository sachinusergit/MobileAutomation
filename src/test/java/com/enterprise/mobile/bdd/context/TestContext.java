package com.enterprise.mobile.bdd.context;

import com.enterprise.mobile.driver.Device;
import com.enterprise.mobile.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Per-scenario state shared by step definitions (driver, device, page
 * objects). Cucumber- picocontainer injects one instance per scenario.
 */
public class TestContext {

    private static final Logger LOG = LoggerFactory.getLogger(TestContext.class);

    private AppiumDriver driver;
    private Device device;

    public void initDriver() {
        LOG.info("Initializing driver for the current scenario thread");
        DriverManager.initDriver();
        driver = DriverManager.getDriver();
        device = DriverManager.getDevice();
    }

    public AppiumDriver getDriver() {
        return driver;
    }

    public Device getDevice() {
        return device;
    }

    public void quitDriver() {
        if (driver != null) {
            DriverManager.quitDriver();
            driver = null;
            device = null;
        }
    }
}
