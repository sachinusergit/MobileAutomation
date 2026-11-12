package com.enterprise.mobile.listeners;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Retries flaky tests a configurable number of times. The retry budget is
 * controlled by {@code retry.count} (default 1). Set it to 0 to disable retries.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAnalyzer.class);

    private final AtomicInteger attempts = new AtomicInteger(0);
    private final int maxRetries = ConfigManager.getInt(FrameworkConstants.RETRY_COUNT,
            FrameworkConstants.DEFAULT_RETRY_COUNT);

    @Override
    public boolean retry(ITestResult result) {
        if (maxRetries <= 0) {
            return false;
        }
        int currentAttempt = attempts.incrementAndGet();
        if (currentAttempt <= maxRetries) {
            LOG.warn("Retrying test '{}' - attempt {}/{}", result.getName(), currentAttempt, maxRetries);
            return true;
        }
        LOG.error("Test '{}' exhausted its retry budget ({} attempts)", result.getName(), maxRetries + 1);
        return false;
    }
}
