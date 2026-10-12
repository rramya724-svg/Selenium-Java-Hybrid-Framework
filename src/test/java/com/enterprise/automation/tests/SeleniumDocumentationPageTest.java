package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.SeleniumContentPage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import com.enterprise.automation.zephyr.ZephyrTestCase;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class SeleniumDocumentationPageTest extends BaseTest {
    private static final String TEST_CASE_ID = "TC_008";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_TOOLING = "tooling";
    private static final String EXPECTED_URL_FRAGMENT = "/documentation";
    private static final String MSG_WRONG_URL = "The browser is not on the Selenium Documentation page";
    private static final String MSG_HEADING_MISSING = "The Selenium Documentation page did not render its main heading";
    private static final String MSG_TITLE_MISMATCH =
            "The Selenium Documentation page title did not contain the expected fragment";

    @ZephyrTestCase("SCRUM-T16")
    @Test(description = "Verify that the Selenium Documentation page loads with its heading and title",
          groups = {GROUP_REGRESSION, GROUP_TOOLING},
          priority = 8)
    public void verifySeleniumDocumentationPage() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);
        logger.info("Test Started : verifySeleniumDocumentationPage");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());
        SeleniumContentPage page = new SeleniumContentPage().openPage(data.applicationUrl());
        String currentUrl = page.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(EXPECTED_URL_FRAGMENT),
                MSG_WRONG_URL + " | actual URL = '" + currentUrl + "'");
        ExtentTestManager.logInfo("Current URL : " + currentUrl);
        Assert.assertTrue(page.isPageHeadingDisplayed(), MSG_HEADING_MISSING);
        ExtentTestManager.logInfo("Page heading : " + page.getPageHeading());
        String actualTitle = page.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);
        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");
        logger.info("Test Passed : verifySeleniumDocumentationPage");
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
