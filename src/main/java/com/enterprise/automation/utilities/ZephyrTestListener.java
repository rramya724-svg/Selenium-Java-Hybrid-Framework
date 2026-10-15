package com.enterprise.automation.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ZephyrTestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(ZephyrTestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
       logger.info("🟢 Test Started: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        updateZephyrExecution(result, "Pass");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        updateZephyrExecution(result, "Fail");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        updateZephyrExecution(result, "Blocked");
    }

    @Override
    public void onStart(ITestContext context) {
       logger.info("🚀 Test Execution Started: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
       logger.info("🏁 Test Execution Finished: " + context.getName());
    }

    private void updateZephyrExecution(ITestResult result, String status) {
        ZephyrTestCase annotation = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(ZephyrTestCase.class);
        if (annotation != null) {
            String testCaseId = annotation.id();
            logger.info("🔄 Updating Zephyr Test Case: {} with status: {}", testCaseId, status);
            UpdateZephyrTestExecution.updateTestExecution(result, status);
        } else {
        	 logger.info("⚠ No Zephyr Test Case ID found for test: {}", result.getName());
        }
    }
}
