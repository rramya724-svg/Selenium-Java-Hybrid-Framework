package com.enterprise.automation.listeners;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.reports.ExecutionSummary;
import com.enterprise.automation.utilities.LoggerUtility;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LoggerUtility.getLogger(RetryAnalyzer.class);
    private final int maxRetryCount =
            ConfigReader.getInt(ConfigKey.RETRY_COUNT, FrameworkConstants.DEFAULT_RETRY_COUNT);
    private int attempt;

    @Override
    public boolean retry(ITestResult result) {
        if (attempt >= maxRetryCount) {
            LOGGER.warn("Retry budget exhausted for [{}] after {} attempt(s)", result.getName(), maxRetryCount);
            return false;
        }
        attempt++;
        ExecutionSummary.incrementRetried();
        LOGGER.warn("Retrying [{}] : attempt {} of {}", result.getName(), attempt, maxRetryCount);
        return true;
    }

    public int getAttempt() {
        return attempt;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }
}
