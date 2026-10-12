package com.enterprise.automation.base;

import com.enterprise.automation.driver.DriverFactory;
import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.enums.BrowserType;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.LoggerUtility;
import com.enterprise.automation.zephyr.ZephyrListener;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

@Listeners(ZephyrListener.class)
public abstract class BaseTest {
    protected final Logger logger = LoggerUtility.getLogger(this.getClass());
    protected static final String TEST_CASE_ID_ATTRIBUTE = "testCaseId";
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 60;
    private static final int SCRIPT_TIMEOUT_SECONDS = 30;
    private static final int IMPLICIT_WAIT_SECONDS = 0;

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional String browserOverride) {
        String testName = currentTestMethodName();
        String threadName = Thread.currentThread().getName();
        logger.info("============================================================");
        logger.info("Starting test [{}] on thread [{}]", testName, threadName);
        try {
            if (browserOverride == null || browserOverride.isBlank()) {
                logger.info("No browser override supplied. Using framework configuration.");
                DriverFactory.initialiseDriver();
            } else {
                BrowserType browser = BrowserType.from(browserOverride);
                logger.info("Browser override received: [{}]", browser);
                DriverFactory.initialiseDriver(browser);
            }
            WebDriver driver = DriverManager.getDriver();
            if (driver == null) {
                throw new IllegalStateException("WebDriver was not initialized for test: " + testName);
            }
            configureTimeouts(driver);
            logger.info("WebDriver initialized successfully for test [{}]", testName);
            logger.info("Browser session started on thread [{}]", threadName);
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver for test [{}]", testName, e);
            safeQuitDriver();
            throw e;
        }
    }

    private void configureTimeouts(WebDriver driver) {
        logger.info(
                "Configuring Selenium timeouts: pageLoad={}s, script={}s, implicit={}s",
                PAGE_LOAD_TIMEOUT_SECONDS,
                SCRIPT_TIMEOUT_SECONDS,
                IMPLICIT_WAIT_SECONDS
        );
        driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().timeouts().scriptTimeout(java.time.Duration.ofSeconds(SCRIPT_TIMEOUT_SECONDS));
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
    }

    private String currentTestMethodName() {
        ITestResult currentResult = Reporter.getCurrentTestResult();
        if (currentResult == null || currentResult.getMethod() == null) {
            return "unknown";
        }
        return currentResult.getMethod().getMethodName();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        String testName = "unknown";
        if (result != null && result.getMethod() != null) {
            testName = result.getMethod().getMethodName();
        }
        String status = getTestStatus(result);
        String threadName = Thread.currentThread().getName();
        logger.info("Test [{}] completed with status [{}]", testName, status);
        logger.info("Starting browser cleanup for test [{}] on thread [{}]", testName, threadName);
        try {
            safeQuitDriver();
        } finally {
            try {
                ExtentTestManager.unload();
                logger.info("Extent test context cleared for [{}]", testName);
            } catch (Exception e) {
                logger.error("Failed to unload Extent test context for [{}]", testName, e);
            }
            logger.info("Cleanup completed for test [{}]", testName);
            logger.info("============================================================");
        }
    }

    private void safeQuitDriver() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver != null) {
                logger.info("Closing WebDriver session on thread [{}]", Thread.currentThread().getName());
                DriverFactory.quitDriver();
                logger.info("WebDriver session closed successfully");
            } else {
                logger.debug("No active WebDriver found for cleanup");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while closing WebDriver", e);
        }
    }

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

    public static WebDriver getDriver() {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is not available for the current thread.");
        }
        return driver;
    }
}
