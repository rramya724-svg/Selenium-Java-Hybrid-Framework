package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.FlipkartHomePage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import com.enterprise.automation.zephyr.ZephyrTestCase;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class FlipkartHomePageTest extends BaseTest {
    private static final String TEST_CASE_ID = "TC_002";
    private static final String GROUP_SMOKE = "smoke";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_E_COMMERCE = "ecommerce";
    private static final String MSG_PAGE_NOT_LOADED = "The Flipkart home page did not render its brand logo";
    private static final String MSG_SEARCH_BOX_MISSING =
            "The Flipkart product search box was not displayed on the home page";
    private static final String MSG_TITLE_MISMATCH =
            "The Flipkart home page title did not contain the expected fragment";

    @ZephyrTestCase("SCRUM-T10")
    @Test(description = "Verify that the Flipkart home page loads and exposes the expected title",
            groups = {GROUP_SMOKE, GROUP_REGRESSION, GROUP_E_COMMERCE},priority = 2)
    public void verifyFlipkartHomePageTitle() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);
        logger.info("============================================================");
        logger.info("Test Started : verifyFlipkartHomePageTitle");
        logger.info("Test Case ID : {}", TEST_CASE_ID);
        logger.info("Application URL : {}", data.applicationUrl());
        logger.info("Expected title fragment : {}", data.expectedTitleFragment());
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());
        FlipkartHomePage flipkartHomePage = new FlipkartHomePage().openHomePage(data.applicationUrl());
        boolean popupDismissed = flipkartHomePage.dismissLoginPopupIfPresent();
        logger.info("Login popup dismissed: {}", popupDismissed);
        boolean homePageLoaded = flipkartHomePage.isHomePageLoaded();
        Assert.assertFalse(homePageLoaded, MSG_PAGE_NOT_LOADED);
        ExtentTestManager.logInfo("Flipkart brand logo rendered successfully");
        boolean searchBoxDisplayed = flipkartHomePage.isSearchBoxDisplayed();
        Assert.assertTrue(searchBoxDisplayed, MSG_SEARCH_BOX_MISSING);
        ExtentTestManager.logInfo("Flipkart search box is available");
        String actualTitle = flipkartHomePage.getPageTitle();
        String expectedTitleFragment = data.expectedTitleFragment();
        logger.info("Actual page title : [{}]", actualTitle);
        logger.info("Expected title fragment : [{}]", expectedTitleFragment);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);
        Assert.assertNotNull(actualTitle, "Flipkart page title must not be null");
        Assert.assertFalse(actualTitle.isBlank(), "Flipkart page title must not be blank");
        Assert.assertTrue(
                actualTitle.toLowerCase().contains(expectedTitleFragment.toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + expectedTitleFragment + "' | actual title = '"
                        + actualTitle + "'"
        );
        ExtentTestManager.logInfo("Expected title fragment was found successfully");
        logger.info("Test Passed : verifyFlipkartHomePageTitle");
        logger.info("============================================================");
    }

    private TestDataRow resolveTestData() {
        return ExcelUtility.findByTestCaseId(TEST_CASE_ID)
                .orElseThrow(
                        () -> new com.enterprise.automation.exceptions.FrameworkException(
                                "Test data row [" + TEST_CASE_ID + "] is missing from testdata.xlsx"
                        )
                );
    }

    private void markTestCaseId(String testCaseId) {
        ITestResult currentResult = Reporter.getCurrentTestResult();
        if (currentResult != null) {
            currentResult.setAttribute(TEST_CASE_ID_ATTRIBUTE, testCaseId);
        }
    }
}
