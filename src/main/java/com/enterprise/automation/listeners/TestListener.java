package com.enterprise.automation.listeners;

import com.aventstack.extentreports.Status;
import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.email.EmailUtility;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.reports.ExecutionSummary;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.reports.ReportManager;
import com.enterprise.automation.utilities.DateUtility;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.LoggerUtility;
import com.enterprise.automation.utilities.ScreenshotUtility;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Optional;

/**
 * Central execution listener: drives Extent reporting, screenshot capture,
 * Excel result write back, the console summary banner and the notification email.
 *
 * <p>Keeping every cross cutting concern here means test classes contain only
 * business steps, which is the single most valuable property of the design.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger LOGGER = LoggerUtility.getLogger(TestListener.class);

    private static final String BANNER_LINE =
            "==============================================================================";
    private static final String TEST_CASE_ID_ATTRIBUTE = "testCaseId";
    private static final String START_MILLIS_ATTRIBUTE = "startMillis";

    /* ------------------------------------------------------------------ */
    /* Suite level                                                         */
    /* ------------------------------------------------------------------ */

    @Override
    public void onStart(ISuite suite) {
        ExecutionSummary.markSuiteStart();
        ReportManager.getInstance();
        LOGGER.info(BANNER_LINE);
        LOGGER.info("SUITE STARTED : {}", suite.getName());
        LOGGER.info("Environment   : {}", ConfigReader.get(ConfigKey.ENVIRONMENT, "QA"));
        LOGGER.info("Browser       : {}", ConfigReader.get(ConfigKey.BROWSER, "chrome"));
        LOGGER.info("Headless      : {}", ConfigReader.getBoolean(ConfigKey.HEADLESS, false));
        LOGGER.info("Started At    : {}", DateUtility.currentDisplayTimestamp());
        LOGGER.info(BANNER_LINE);
    }

    @Override
    public void onFinish(ISuite suite) {
        ExecutionSummary.markSuiteEnd();
        ReportManager.flush();
        logExecutionSummary(suite.getName());
        EmailUtility.sendExecutionReport();
    }

    /* ------------------------------------------------------------------ */
    /* Test level                                                          */
    /* ------------------------------------------------------------------ */

    @Override
    public void onTestStart(ITestResult result) {
        result.setAttribute(START_MILLIS_ATTRIBUTE, System.currentTimeMillis());

        String testName = result.getMethod().getMethodName();
        String description = Optional.ofNullable(result.getMethod().getDescription())
                                     .orElse(FrameworkConstants.EMPTY_STRING);

        ExtentTestManager.startTest(testName, description);
        ExtentTestManager.assignCategories(result.getTestClass().getRealClass().getSimpleName());
        ExtentTestManager.logInfo("Execution started on thread [" + Thread.currentThread().getName() + "]");

        LOGGER.info("TEST STARTED : {} on thread [{}]", testName, Thread.currentThread().getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long elapsed = elapsedMillis(result);
        ExecutionSummary.incrementPassed();

        ExtentTestManager.logPass("Test passed in " + DateUtility.formatDuration(elapsed));

        String screenshotPath = null;
        if (ConfigReader.getBoolean(ConfigKey.SCREENSHOT_ON_SUCCESS, true)) {
            screenshotPath = captureAndAttach(result, Status.PASS, "Screenshot on success");
        }

        writeResultToExcel(result, FrameworkConstants.STATUS_PASS, elapsed, screenshotPath);
        LOGGER.info("TEST PASSED  : {} | duration = {}",
                result.getMethod().getMethodName(), DateUtility.formatDuration(elapsed));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long elapsed = elapsedMillis(result);
        ExecutionSummary.incrementFailed();

        ExtentTestManager.logFail("Test failed in " + DateUtility.formatDuration(elapsed));
        ExtentTestManager.logThrowable(result.getThrowable());

        String screenshotPath = null;
        if (ConfigReader.getBoolean(ConfigKey.SCREENSHOT_ON_FAILURE, true)) {
            screenshotPath = captureAndAttach(result, Status.FAIL, "Screenshot on failure");
        }

        writeResultToExcel(result, FrameworkConstants.STATUS_FAIL, elapsed, screenshotPath);
        LOGGER.error("TEST FAILED  : {} | duration = {}",
                result.getMethod().getMethodName(), DateUtility.formatDuration(elapsed), result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        long elapsed = elapsedMillis(result);
        ExecutionSummary.incrementSkipped();

        ExtentTestManager.logSkip("Test skipped");
        if (result.getThrowable() != null) {
            ExtentTestManager.logThrowable(result.getThrowable());
        }

        writeResultToExcel(result, FrameworkConstants.STATUS_SKIP, elapsed, null);
        LOGGER.warn("TEST SKIPPED : {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        ExtentTestManager.logWarning("Test failed but stayed within the configured success percentage");
        LOGGER.warn("TEST FAILED WITHIN SUCCESS PERCENTAGE : {}", result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentTestManager.unload();
        LOGGER.info("TEST CONTEXT FINISHED : {} | passed = {}, failed = {}, skipped = {}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    /* ------------------------------------------------------------------ */
    /* Helpers                                                             */
    /* ------------------------------------------------------------------ */

    private String captureAndAttach(ITestResult result, Status status, String caption) {
        String testName = result.getMethod().getMethodName();

        ScreenshotUtility.captureAsBase64()
                .ifPresent(base64 -> ExtentTestManager.attachBase64Screenshot(status, base64, caption));

        return ScreenshotUtility.captureToFile(testName).orElse(null);
    }

    private void writeResultToExcel(ITestResult result, String status, long elapsed, String screenshotPath) {
        Object attribute = result.getAttribute(TEST_CASE_ID_ATTRIBUTE);
        if (attribute == null) {
            LOGGER.debug("No testCaseId attribute set for [{}]; Excel write back skipped",
                    result.getMethod().getMethodName());
            return;
        }
        ExcelUtility.writeExecutionResult(String.valueOf(attribute), status, elapsed, screenshotPath);
    }

    private long elapsedMillis(ITestResult result) {
        Object startMillis = result.getAttribute(START_MILLIS_ATTRIBUTE);
        if (startMillis instanceof Long start) {
            return System.currentTimeMillis() - start;
        }
        return result.getEndMillis() - result.getStartMillis();
    }

    private void logExecutionSummary(String suiteName) {
        LOGGER.info(BANNER_LINE);
        LOGGER.info("SUITE FINISHED : {}", suiteName);
        LOGGER.info("Total Tests    : {}", ExecutionSummary.getTotal());
        LOGGER.info("Passed         : {}", ExecutionSummary.getPassed());
        LOGGER.info("Failed         : {}", ExecutionSummary.getFailed());
        LOGGER.info("Skipped        : {}", ExecutionSummary.getSkipped());
        LOGGER.info("Retries        : {}", ExecutionSummary.getRetried());
        LOGGER.info("Pass Rate      : {}%", ExecutionSummary.getPassPercentage());
        LOGGER.info("Total Duration : {}", DateUtility.formatDuration(ExecutionSummary.getDurationMillis()));
        LOGGER.info("Extent Report  : {}", ReportManager.getReportFilePath());
        LOGGER.info("Finished At    : {}", DateUtility.currentDisplayTimestamp());
        LOGGER.info(BANNER_LINE);
    }
}
