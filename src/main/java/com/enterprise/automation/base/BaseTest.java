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
 * Superclass of every test class in the framework.
 *
 * <p>Because the suite runs with {@code parallel="methods"}, driver setup and
 * teardown are bound to {@code @BeforeMethod} and {@code @AfterMethod}: each
 * test method therefore owns a private browser for its entire lifetime and
 * releases it immediately afterwards. Teardown runs inside {@code alwaysRun}
 * so a browser is closed even when setup or the test itself explodes.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public abstract class BaseTest {

    /** Logger available to every subclass. */
    protected final Logger logger = LoggerUtility.getLogger(this.getClass());

    /** Attribute key used by the listener to write results back into Excel. */
    protected static final String TEST_CASE_ID_ATTRIBUTE = "testCaseId";

    /**
     * Launches the browser before every test method.
     *
     * <p>The single {@code @Optional} parameter lets a suite XML select a browser
     * per {@code <test>} block. When it is absent the browser configured in
     * {@code config.properties} (or {@code -Dbrowser}) is used instead.</p>
     *
     * @param browserOverride optional browser supplied by the TestNG XML parameter
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional String browserOverride) {
        if (browserOverride == null || browserOverride.isBlank()) {
            DriverFactory.initialiseDriver();
        } else {
            DriverFactory.initialiseDriver(BrowserType.from(browserOverride));
        }
        logger.info("Browser ready for [{}] on thread [{}]",
                currentTestMethodName(), Thread.currentThread().getName());
    }

    private String currentTestMethodName() {
        ITestResult currentResult = Reporter.getCurrentTestResult();
        return currentResult == null ? "unknown" : currentResult.getMethod().getMethodName();
    }

    /**
     * Closes the browser after every test method and clears the thread local
     * report node so no state leaks into the next method executed by this thread.
     *
     * @param result the finished TestNG result, used only for logging
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        logger.info("Tearing down [{}] on thread [{}]",
                result.getMethod().getMethodName(), Thread.currentThread().getName());
        DriverFactory.quitDriver();
        ExtentTestManager.unload();
    }

    /**
     * Convenience accessor for the driver of the current thread.
     *
     * @return the active {@link WebDriver}
     */
    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }
}
