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

public class WikipediaSearchTest extends BaseTest {
    private static final String TEST_CASE_ID = "TC_005";
    private static final String GROUP_SMOKE = "smoke";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_CONTENT = "content";
    private static final String SEARCH_TERM = "Albert Einstein";
    private static final String ARTICLE_URL_FRAGMENT = "wikipedia.org/wiki/";
    private static final String MSG_SEARCH_INPUT_MISSING = "The Wikipedia search input was not displayed on the portal";
    private static final String MSG_NOT_ON_ARTICLE = "Search did not navigate to a Wikipedia article page";
    private static final String MSG_TITLE_MISMATCH =
            "The Wikipedia article title did not contain the expected fragment";

    @ZephyrTestCase("SCRUM-T13")
    @Test(description = "Verify that searching from the Wikipedia portal opens the matching article",
          groups = {GROUP_SMOKE, GROUP_REGRESSION, GROUP_CONTENT},
          priority = 5)
    public void verifyWikipediaArticleSearch() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);
        logger.info("Test Started : verifyWikipediaArticleSearch");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());
        WikipediaHomePage page = new WikipediaHomePage().openHomePage(data.applicationUrl());
        Assert.assertTrue(page.isSearchInputDisplayed(), MSG_SEARCH_INPUT_MISSING);
        ExtentTestManager.logInfo("Wikipedia search input is available");
        page.searchWithEnterKey(SEARCH_TERM);
        ExtentTestManager.logInfo("Searched for : " + SEARCH_TERM);
        String currentUrl = page.getCurrentUrl();
        logger.info("Current URL : {}", currentUrl);
        Assert.assertTrue(currentUrl.contains(ARTICLE_URL_FRAGMENT),
                MSG_NOT_ON_ARTICLE + " | actual URL = '" + currentUrl + "'");
        ExtentTestManager.logInfo("Landed on article URL : " + currentUrl);
        String actualTitle = page.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);
        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");
        logger.info("Test Passed : verifyWikipediaArticleSearch");
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
