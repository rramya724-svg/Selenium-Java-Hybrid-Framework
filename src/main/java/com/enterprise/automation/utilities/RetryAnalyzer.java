package com.enterprise.automation.utilities;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.enterprise.automation.base.BaseTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RetryAnalyzer implements IRetryAnalyzer {
	private int retryAttemptsCounter = 0;
	// Renamed maxRetryLimit to MAX_RETRY_LIMIT as per SonarQube rule
	private static final int MAX_RETRY_LIMIT = 3;
	private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);

	// Constants for duplicated literals
	private static final String ATTRIBUTE_RETRY_COUNT = "RETRY_COUNT";
	private static final String ATTRIBUTE_ZEPHYR_TEST_CASE_ID = "zephyrTestCaseId";
	private static final String ZEPHYR_STATUS_FAIL = "Fail";
	private static final String ZEPHYR_TEST_CASE_ID_NOT_FOUND_WARN = "⚠ Zephyr Test Case ID not found for: {}";
	private static final String ZEPHYR_UPDATE_LOG = "❌ Updating Zephyr: Test Case ID - {} | Status - {}";
	private static final String BROWSER_CLOSED_LOG = "🔴 Browser closed before retry attempt: {}";
	private static final String BROWSER_CLOSE_EXCEPTION_WARN = "⚠ Exception while closing the browser: {}";


	public synchronized boolean retry(ITestResult result) {
		if (!result.isSuccess() && result.getStatus() != ITestResult.SKIP
				&& result.getStatus() != ITestResult.SUCCESS_PERCENTAGE_FAILURE) {

			result.setAttribute(ATTRIBUTE_RETRY_COUNT, retryAttemptsCounter + 1); // 1-based count

			if (retryAttemptsCounter < MAX_RETRY_LIMIT) { // Use the renamed constant
				retryAttemptsCounter++;
				closeBrowser();
				return true;
			} else {
				updateZephyrAsFailed(result); // Final failure
			}
		}
		return false;
	}

	private void updateZephyrAsFailed(ITestResult result) {
		String testCaseId = (String) result.getAttribute(ATTRIBUTE_ZEPHYR_TEST_CASE_ID); // Use constant

		if (testCaseId == null) {
			logger.info(ZEPHYR_TEST_CASE_ID_NOT_FOUND_WARN, result.getMethod().getMethodName()); // Use constant
			return;
		}

		String status = ZEPHYR_STATUS_FAIL; // Use constant
		logger.info(ZEPHYR_UPDATE_LOG, testCaseId, status); // Use constant

		try {
			// Pass the full `ITestResult` object instead of just testCaseId
			UpdateZephyrTestExecution.updateTestExecution(result, status);
		} catch (Exception e) {
			  logger.warn("Added for exeption");
		}
	}

	private void closeBrowser() {
		try {
			if (BaseTest.getDriver() != null) {
				BaseTest.getDriver().quit();
				 logger.info(BROWSER_CLOSED_LOG, retryAttemptsCounter); // Use constant
			}
		} catch (Exception e) {
	        logger.warn(BROWSER_CLOSE_EXCEPTION_WARN, e.getMessage(), e); // Use constant
		}
	}
}
