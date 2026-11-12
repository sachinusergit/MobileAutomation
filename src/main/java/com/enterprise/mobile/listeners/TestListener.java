package com.enterprise.mobile.listeners;

import com.enterprise.mobile.driver.DriverManager;
import com.enterprise.mobile.driver.Device;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Framework wide TestNG listener providing structured logging and Allure
 * metadata. Screenshots are handled by {@code BaseTest} while the driver is
 * guaranteed to be alive.
 */
public class TestListener implements ITestListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        LOG.info("Suite '{}' started", context.getSuite().getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info(">>> START {}.{}", result.getTestClass().getRealClass().getSimpleName(), result.getName());
        attachDevice(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("<<< PASS  {} ({} ms)", result.getName(), duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("<<< FAIL  {} ({} ms)", result.getName(), duration(result), result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("<<< SKIP  {}: {}", result.getName(),
                result.getThrowable() == null ? "no reason supplied" : result.getThrowable().getMessage());
    }

    @Override
    public void onFinish(ITestContext context) {
        LOG.info("Suite '{}' finished | passed={}, failed={}, skipped={}",
                context.getSuite().getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    private void attachDevice(ITestResult result) {
        if (!DriverManager.hasDriver()) {
            return;
        }
        Device device = DriverManager.getDevice();
        Allure.addAttachment("Device", "text/plain", device.displayName());
        Allure.addAttachment("Platform", "text/plain", device.platformEnum().value());
    }

    private long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
