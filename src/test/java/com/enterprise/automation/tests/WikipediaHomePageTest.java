package com.enterprise.automation.tests;

import com.enterprise.automation.base.BaseTest;
import com.enterprise.automation.pages.WikipediaHomePage;
import com.enterprise.automation.reports.ExtentTestManager;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.TestDataRow;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test Case 3 : launch Chrome, open the Wikipedia portal, validate the page
 * title, capture a screenshot and close the browser.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class WikipediaHomePageTest extends BaseTest {

    private static final String TEST_CASE_ID = "TC_003";
    private static final String GROUP_SMOKE = "smoke";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_CONTENT = "content";

    private static final int EXPECTED_MINIMUM_LANGUAGES = 5;

    private static final String MSG_TITLE_MISMATCH =
            "The Wikipedia portal title did not contain the expected fragment";
    private static final String MSG_PAGE_NOT_LOADED =
            "The Wikipedia portal did not render its central globe logo";
    private static final String MSG_SEARCH_INPUT_MISSING =
            "The Wikipedia search input was not displayed on the portal";
    private static final String MSG_LANGUAGES_MISSING =
            "The Wikipedia portal did not list the expected number of featured languages";

    /**
     * Validates that the Wikipedia portal loads and reports the expected title.
     */
    @Test(description = "Verify that the Wikipedia portal loads and exposes the expected title",
          groups = {GROUP_SMOKE, GROUP_REGRESSION, GROUP_CONTENT},
          priority = 3)
    public void verifyWikipediaHomePageTitle() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);

        logger.info("Test Started : verifyWikipediaHomePageTitle");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());

        WikipediaHomePage wikipediaHomePage = new WikipediaHomePage().openHomePage(data.applicationUrl());

        Assert.assertTrue(wikipediaHomePage.isHomePageLoaded(), MSG_PAGE_NOT_LOADED);
        ExtentTestManager.logInfo("Wikipedia central logo rendered successfully");

        Assert.assertTrue(wikipediaHomePage.isSearchInputDisplayed(), MSG_SEARCH_INPUT_MISSING);
        ExtentTestManager.logInfo("Wikipedia search input is available");

        List<String> languages = wikipediaHomePage.getFeaturedLanguages();
        logger.info("Featured languages found : {}", languages);
        Assert.assertTrue(languages.size() >= EXPECTED_MINIMUM_LANGUAGES,
                MSG_LANGUAGES_MISSING + " | found = " + languages.size());
        ExtentTestManager.logInfo("Featured languages listed : " + languages.size());

        String actualTitle = wikipediaHomePage.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);

        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");

        logger.info("Test Passed : verifyWikipediaHomePageTitle");
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
