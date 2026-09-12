package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.FlipkartHomePage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Test Case 2 : launch Chrome, open Flipkart, validate the page title,
 * capture a screenshot and close the browser.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class FlipkartHomePageTest extends BaseTest {

    private static final String TEST_CASE_ID = "TC_002";
    private static final String GROUP_SMOKE = "smoke";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_E_COMMERCE = "ecommerce";

    private static final String MSG_TITLE_MISMATCH =
            "The Flipkart home page title did not contain the expected fragment";
    private static final String MSG_PAGE_NOT_LOADED =
            "The Flipkart home page did not render its brand logo";
    private static final String MSG_SEARCH_BOX_MISSING =
            "The Flipkart product search box was not displayed on the home page";

    /**
     * Validates that the Flipkart home page loads and reports the expected title.
     */
    @Test(description = "Verify that the Flipkart home page loads and exposes the expected title",
          groups = {GROUP_SMOKE, GROUP_REGRESSION, GROUP_E_COMMERCE},
          priority = 2)
    public void verifyFlipkartHomePageTitle() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);

        logger.info("Test Started : verifyFlipkartHomePageTitle");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());

        FlipkartHomePage flipkartHomePage = new FlipkartHomePage().openHomePage(data.applicationUrl());
        flipkartHomePage.dismissLoginPopupIfPresent();

        Assert.assertTrue(flipkartHomePage.isHomePageLoaded(), MSG_PAGE_NOT_LOADED);
        ExtentTestManager.logInfo("Flipkart brand logo rendered successfully");

        Assert.assertTrue(flipkartHomePage.isSearchBoxDisplayed(), MSG_SEARCH_BOX_MISSING);
        ExtentTestManager.logInfo("Flipkart search box is available");

        String actualTitle = flipkartHomePage.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);

        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");

        logger.info("Test Passed : verifyFlipkartHomePageTitle");
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
