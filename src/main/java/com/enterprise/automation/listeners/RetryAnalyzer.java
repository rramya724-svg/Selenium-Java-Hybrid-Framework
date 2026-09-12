package com.enterprise.automation.listeners;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.reports.ExecutionSummary;
import com.enterprise.automation.utilities.LoggerUtility;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to the configured limit.
 *
 * <p>TestNG creates one analyzer instance per test method, so the {@code attempt}
 * counter below is naturally confined to a single method and needs no
 * synchronisation even under {@code parallel="methods"}.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOGGER = LoggerUtility.getLogger(RetryAnalyzer.class);

    private final int maxRetryCount =
            ConfigReader.getInt(ConfigKey.RETRY_COUNT, FrameworkConstants.DEFAULT_RETRY_COUNT);

    private int attempt;

    /**
     * Decides whether the failed test should be executed again.
     *
     * @param result the failed result reported by TestNG
     * @return {@code true} while the retry budget has not been exhausted
     */
    @Override
    public boolean retry(ITestResult result) {
        if (attempt >= maxRetryCount) {
            LOGGER.warn("Retry budget exhausted for [{}] after {} attempt(s)",
                    result.getName(), maxRetryCount);
            return false;
        }
        attempt++;
        ExecutionSummary.incrementRetried();
        LOGGER.warn("Retrying [{}] : attempt {} of {}", result.getName(), attempt, maxRetryCount);
        return true;
    }

    /**
     * @return how many retries have already been consumed
     */
    public int getAttempt() {
        return attempt;
    }

    /**
     * @return the configured retry ceiling
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }
}
