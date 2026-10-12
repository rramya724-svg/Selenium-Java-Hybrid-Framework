package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.WikipediaHomePage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import com.enterprise.automation.zephyr.ZephyrTestCase;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class WikipediaPortalTaglineTitleTest extends BaseTest {
    private static final String TEST_CASE_ID = "TC_009";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_CONTENT = "content";
    private static final String MSG_PAGE_NOT_LOADED = "The Wikipedia portal did not render its central globe logo";
    private static final String MSG_TITLE_MISMATCH = "The Wikipedia portal title did not contain the expected tagline";

    @ZephyrTestCase("SCRUM-T17")
    @Test(description = "Verify that the Wikipedia portal title contains the tagline 'The Free Encyclopedia'",
          groups = {GROUP_REGRESSION, GROUP_CONTENT},
          priority = 9)
    public void verifyWikipediaPortalTaglineInTitle() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);
        logger.info("Test Started : verifyWikipediaPortalTaglineInTitle");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());
        WikipediaHomePage page = new WikipediaHomePage().openHomePage(data.applicationUrl());
        Assert.assertTrue(page.isHomePageLoaded(), MSG_PAGE_NOT_LOADED);
        ExtentTestManager.logInfo("Wikipedia central logo rendered successfully");
        String actualTitle = page.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);
        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");
        logger.info("Test Passed : verifyWikipediaPortalTaglineInTitle");
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
