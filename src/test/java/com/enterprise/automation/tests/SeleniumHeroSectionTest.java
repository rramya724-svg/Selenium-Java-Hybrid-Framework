package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.SeleniumHomePage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import com.enterprise.automation.zephyr.ZephyrTestCase;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class SeleniumHeroSectionTest extends BaseTest {
    private static final String TEST_CASE_ID = "TC_007";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_TOOLING = "tooling";
    private static final String EXPECTED_HEADING_FRAGMENT = "selenium";
    private static final String MSG_HEADING_MISMATCH = "The Selenium hero heading did not mention Selenium";
    private static final String MSG_DOWNLOADS_MISSING =
            "The Downloads link was not displayed in the Selenium navigation bar";
    private static final String MSG_TITLE_MISMATCH =
            "The Selenium home page title did not contain the expected fragment";

    @ZephyrTestCase("SCRUM-T15")
    @Test(description = "Verify the selenium.dev hero heading and Downloads entry point",
          groups = {GROUP_REGRESSION, GROUP_TOOLING},
          priority = 7)
    public void verifySeleniumHeroSection() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);
        logger.info("Test Started : verifySeleniumHeroSection");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());
        SeleniumHomePage page = new SeleniumHomePage().openHomePage(data.applicationUrl());
        String heroHeading = page.getHeroHeading();
        logger.info("Hero heading : {}", heroHeading);
        Assert.assertTrue(heroHeading.toLowerCase().contains(EXPECTED_HEADING_FRAGMENT),
                MSG_HEADING_MISMATCH + " | actual heading = '" + heroHeading + "'");
        ExtentTestManager.logInfo("Hero heading : " + heroHeading);
        Assert.assertTrue(page.isDownloadsLinkDisplayed(), MSG_DOWNLOADS_MISSING);
        ExtentTestManager.logInfo("Downloads entry point is available");
        String actualTitle = page.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);
        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");
        logger.info("Test Passed : verifySeleniumHeroSection");
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
