package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.SeleniumHomePage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Test Case 4 : launch Chrome, open selenium.dev, validate the page title,
 * capture a screenshot and close the browser.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class SeleniumHomePageTest extends BaseTest {

    private static final String TEST_CASE_ID = "TC_004";
    private static final String GROUP_SMOKE = "smoke";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_TOOLING = "tooling";

    private static final String MSG_TITLE_MISMATCH =
            "The Selenium home page title did not contain the expected fragment";
    private static final String MSG_PAGE_NOT_LOADED =
            "The Selenium home page did not render its navigation logo";
    private static final String MSG_DOCUMENTATION_MISSING =
            "The Documentation link was not displayed in the Selenium navigation bar";
    private static final String MSG_DOWNLOADS_MISSING =
            "The Downloads link was not displayed in the Selenium navigation bar";

    /**
     * Validates that the Selenium project site loads and reports the expected title.
     */
    @Test(description = "Verify that the Selenium project site loads and exposes the expected title",
          groups = {GROUP_SMOKE, GROUP_REGRESSION, GROUP_TOOLING},
          priority = 4)
    public void verifySeleniumHomePageTitle() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);

        logger.info("Test Started : verifySeleniumHomePageTitle");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());

        SeleniumHomePage seleniumHomePage = new SeleniumHomePage().openHomePage(data.applicationUrl());

        Assert.assertTrue(seleniumHomePage.isHomePageLoaded(), MSG_PAGE_NOT_LOADED);
        ExtentTestManager.logInfo("Selenium navigation logo rendered successfully");

        Assert.assertTrue(seleniumHomePage.isDocumentationLinkDisplayed(), MSG_DOCUMENTATION_MISSING);
        Assert.assertTrue(seleniumHomePage.isDownloadsLinkDisplayed(), MSG_DOWNLOADS_MISSING);
        ExtentTestManager.logInfo("Documentation and Downloads entry points are available");

        String actualTitle = seleniumHomePage.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);

        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");

        logger.info("Test Passed : verifySeleniumHomePageTitle");
    }

    private TestDataRow resolveTestData() {
        return ExcelUtility.findByTestCaseId(TEST_CASE_ID)
                .orElseThrow(() -> new com.enterprise.automation.exceptions.FrameworkException(
                        "Test data row [" + TEST_CASE_ID + "] is missing from testdata.xlsx"));
    }

    private void markTestCaseId(String testCaseId) {
        ITestResult currentResult = Reporter.getCurrentTestResult();
        if (currentResult != null) {
            currentResult.setAttribute(TEST_CASE_ID_ATTRIBUTE, testCaseId);
        }
    }
}
