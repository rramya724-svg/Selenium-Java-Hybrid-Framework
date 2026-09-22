package com.enterprise.automation.base;

import com.enterprise.automation.driver.DriverFactory;
import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.enums.BrowserType;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.LoggerUtility;

import org.apache.logging.log4j.Logger;

import org.openqa.selenium.WebDriver;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base test class for all automation test classes.
 *
 * <p>
 * Responsibilities:
 * </p>
 * <ul>
 *     <li>Creates a WebDriver before every test method.</li>
 *     <li>Maintains thread-safe driver usage through DriverManager.</li>
 *     <li>Applies standard Selenium timeout configuration.</li>
 *     <li>Logs test and browser lifecycle events.</li>
 *     <li>Always closes the browser after every test.</li>
 *     <li>Prevents browser/session leakage between TestNG methods.</li>
 *     <li>Works with parallel TestNG execution.</li>
 * </ul>
 *
 * @author Lalith Kumar BV
 * @version 2.0
 */
public abstract class BaseTest {

    /**
     * Logger available to all child test classes.
     */
    protected final Logger logger =
            LoggerUtility.getLogger(this.getClass());

    /**
     * Test case ID attribute used by listeners/reporting utilities.
     */
    protected static final String TEST_CASE_ID_ATTRIBUTE = "testCaseId";

    /**
     * Maximum time allowed for a page load.
     *
     * <p>
     * This prevents a browser navigation from waiting indefinitely.
     * </p>
     */
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 60;

    /**
     * Maximum time allowed for asynchronous JavaScript execution.
     */
    private static final int SCRIPT_TIMEOUT_SECONDS = 30;

    /**
     * Implicit wait.
     *
     * <p>
     * Keep this at zero when the framework uses explicit waits.
     * Mixing large implicit waits with explicit waits can make failures
     * unnecessarily slow.
     * </p>
     */
    private static final int IMPLICIT_WAIT_SECONDS = 0;

    /**
     * Creates a WebDriver before every TestNG test method.
     *
     * <p>
     * A separate driver is created for each test method. This is important
     * when TestNG is configured for parallel method execution.
     * </p>
     *
     * @param browserOverride optional browser supplied through testng.xml
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional String browserOverride) {

        String testName = currentTestMethodName();
        String threadName = Thread.currentThread().getName();

        logger.info(
                "============================================================"
        );

        logger.info(
                "Starting test [{}] on thread [{}]",
                testName,
                threadName
        );

        try {

            /*
             * Create the driver.
             *
             * Browser from testng.xml takes priority.
             * Otherwise DriverFactory uses the framework configuration.
             */
            if (browserOverride == null || browserOverride.isBlank()) {

                logger.info(
                        "No browser override supplied. Using framework configuration."
                );

                DriverFactory.initialiseDriver();

            } else {

                BrowserType browser =
                        BrowserType.from(browserOverride);

                logger.info(
                        "Browser override received: [{}]",
                        browser
                );

                DriverFactory.initialiseDriver(browser);
            }

            /*
             * Verify that the driver was actually created.
             */
            WebDriver driver = DriverManager.getDriver();

            if (driver == null) {
                throw new IllegalStateException(
                        "WebDriver was not initialized for test: " + testName
                );
            }

            /*
             * Apply standard Selenium timeouts.
             *
             * These are framework-level safeguards.
             */
            configureTimeouts(driver);

            logger.info(
                    "WebDriver initialized successfully for test [{}]",
                    testName
            );

            logger.info(
                    "Browser session started on thread [{}]",
                    threadName
            );

        } catch (Exception e) {

            logger.error(
                    "Failed to initialize WebDriver for test [{}]",
                    testName,
                    e
            );

            /*
             * If driver creation partially succeeded, make sure
             * the browser is not left running.
             */
            safeQuitDriver();

            throw e;
        }
    }

    /**
     * Applies standard Selenium timeout configuration.
     *
     * @param driver active WebDriver
     */
    private void configureTimeouts(WebDriver driver) {

        logger.info(
                "Configuring Selenium timeouts: pageLoad={}s, script={}s, implicit={}s",
                PAGE_LOAD_TIMEOUT_SECONDS,
                SCRIPT_TIMEOUT_SECONDS,
                IMPLICIT_WAIT_SECONDS
        );

        driver.manage()
                .timeouts()
                .pageLoadTimeout(
                        java.time.Duration.ofSeconds(
                                PAGE_LOAD_TIMEOUT_SECONDS
                        )
                );

        driver.manage()
                .timeouts()
                .scriptTimeout(
                        java.time.Duration.ofSeconds(
                                SCRIPT_TIMEOUT_SECONDS
                        )
                );

        driver.manage()
                .timeouts()
                .implicitlyWait(
                        java.time.Duration.ofSeconds(
                                IMPLICIT_WAIT_SECONDS
                        )
                );
    }

    /**
     * Returns the current TestNG method name.
     *
     * @return current test method name
     */
    private String currentTestMethodName() {

        ITestResult currentResult =
                Reporter.getCurrentTestResult();

        if (currentResult == null ||
                currentResult.getMethod() == null) {

            return "unknown";
        }

        return currentResult
                .getMethod()
                .getMethodName();
    }

    /**
     * Tears down the browser after every test method.
     *
     * <p>
     * alwaysRun=true ensures cleanup happens even when:
     * </p>
     * <ul>
     *     <li>test fails</li>
     *     <li>assertion fails</li>
     *     <li>setup fails partially</li>
     *     <li>an unexpected exception occurs</li>
     * </ul>
     *
     * @param result TestNG test result
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {

        String testName = "unknown";

        if (result != null &&
                result.getMethod() != null) {

            testName =
                    result.getMethod().getMethodName();
        }

        String status =
                getTestStatus(result);

        String threadName =
                Thread.currentThread().getName();

        logger.info(
                "Test [{}] completed with status [{}]",
                testName,
                status
        );

        logger.info(
                "Starting browser cleanup for test [{}] on thread [{}]",
                testName,
                threadName
        );

        try {

            safeQuitDriver();

        } finally {

            /*
             * Always clear Extent reporting state even if
             * browser cleanup encounters a problem.
             */
            try {

                ExtentTestManager.unload();

                logger.info(
                        "Extent test context cleared for [{}]",
                        testName
                );

            } catch (Exception e) {

                logger.error(
                        "Failed to unload Extent test context for [{}]",
                        testName,
                        e
                );
            }

            logger.info(
                    "Cleanup completed for test [{}]",
                    testName
            );

            logger.info(
                    "============================================================"
            );
        }
    }

    /**
     * Safely quits the current WebDriver.
     *
     * <p>
     * This method is intentionally defensive because browser cleanup
     * must never prevent the remaining TestNG lifecycle from completing.
     * </p>
     */
    private void safeQuitDriver() {

        try {

            WebDriver driver =
                    DriverManager.getDriver();

            if (driver != null) {

                logger.info(
                        "Closing WebDriver session on thread [{}]",
                        Thread.currentThread().getName()
                );

                DriverFactory.quitDriver();

                logger.info(
                        "WebDriver session closed successfully"
                );

            } else {

                logger.debug(
                        "No active WebDriver found for cleanup"
                );
            }

        } catch (Exception e) {

            logger.error(
                    "Exception occurred while closing WebDriver",
                    e
            );
        }
    }

    /**
     * Converts TestNG result status into a readable log value.
     *
     * @param result TestNG result
     * @return readable test status
     */
    private String getTestStatus(ITestResult result) {

        if (result == null) {
            return "UNKNOWN";
        }

        switch (result.getStatus()) {

            case ITestResult.SUCCESS:
                return "PASSED";

            case ITestResult.FAILURE:
                return "FAILED";

            case ITestResult.SKIP:
                return "SKIPPED";

            default:
                return "UNKNOWN";
        }
    }

    /**
     * Convenience accessor for the WebDriver associated with
     * the current execution thread.
     *
     * @return active WebDriver
     */
    protected WebDriver getDriver() {

        WebDriver driver =
                DriverManager.getDriver();

        if (driver == null) {

            throw new IllegalStateException(
                    "WebDriver is not available for the current thread."
            );
        }

        return driver;
    }
}