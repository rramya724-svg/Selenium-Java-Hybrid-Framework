package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.AmazonHomePage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Test Case 1 : launch Chrome, open Amazon India, validate the page title,
 * capture a screenshot and close the browser.
 *
 * <p>Screenshot capture and browser teardown are intentionally absent from the
 * test body. The listener captures the screenshot on success and on failure,
 * and {@code BaseTest} closes the browser in {@code @AfterMethod}, so the test
 * reads as pure business intent.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class AmazonHomePageTest extends BaseTest {

    private static final String TEST_CASE_ID = "TC_001";
    private static final String GROUP_SMOKE = "smoke";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_E_COMMERCE = "ecommerce";

    private static final String MSG_TITLE_MISMATCH =
            "The Amazon home page title did not contain the expected fragment";
    private static final String MSG_PAGE_NOT_LOADED =
            "The Amazon home page did not render its header logo";
    private static final String MSG_SEARCH_BOX_MISSING =
            "The Amazon search box was not displayed on the home page";

    /**
     * Validates that the Amazon India home page loads and reports the expected title.
     */
    @Test(description = "Verify that the Amazon India home page loads and exposes the expected title",
          groups = {GROUP_SMOKE, GROUP_REGRESSION, GROUP_E_COMMERCE},
          priority = 1)
    public void verifyAmazonHomePageTitle() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);

        logger.info("Test Started : verifyAmazonHomePageTitle");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());

        AmazonHomePage amazonHomePage = new AmazonHomePage().openHomePage(data.applicationUrl());

        Assert.assertTrue(amazonHomePage.isHomePageLoaded(), MSG_PAGE_NOT_LOADED);
        ExtentTestManager.logInfo("Amazon header logo rendered successfully");

        Assert.assertTrue(amazonHomePage.isSearchBoxDisplayed(), MSG_SEARCH_BOX_MISSING);
        ExtentTestManager.logInfo("Amazon search box is available");

        String actualTitle = amazonHomePage.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);

        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");

        logger.info("Test Passed : verifyAmazonHomePageTitle");
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
