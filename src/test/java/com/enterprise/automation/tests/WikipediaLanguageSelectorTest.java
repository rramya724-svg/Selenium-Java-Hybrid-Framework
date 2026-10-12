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
import java.util.List;

public class WikipediaLanguageSelectorTest extends BaseTest {
    private static final String TEST_CASE_ID = "TC_006";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_CONTENT = "content";
    private static final String EXPECTED_LANGUAGE = "English";
    private static final String MSG_SELECTOR_MISSING = "The Wikipedia search language selector was not displayed";
    private static final String MSG_LANGUAGE_MISSING =
            "The featured language list did not include the expected language";
    private static final String MSG_TITLE_MISMATCH = "The Wikipedia portal title did not contain the expected fragment";

    @ZephyrTestCase("SCRUM-T14")
    @Test(description = "Verify the Wikipedia language selector and that English is a featured language",
          groups = {GROUP_REGRESSION, GROUP_CONTENT},
          priority = 6)
    public void verifyWikipediaLanguageSelector() {
        TestDataRow data = resolveTestData();
        markTestCaseId(TEST_CASE_ID);
        logger.info("Test Started : verifyWikipediaLanguageSelector");
        ExtentTestManager.logInfo("Opening " + data.applicationUrl());
        WikipediaHomePage page = new WikipediaHomePage().openHomePage(data.applicationUrl());
        Assert.assertTrue(page.isLanguageSelectorDisplayed(), MSG_SELECTOR_MISSING);
        ExtentTestManager.logInfo("Search language selector is available");
        List<String> languages = page.getFeaturedLanguages();
        logger.info("Featured languages found : {}", languages);
        Assert.assertTrue(languages.contains(EXPECTED_LANGUAGE),
                MSG_LANGUAGE_MISSING + " | expected = '" + EXPECTED_LANGUAGE + "' | found = " + languages);
        ExtentTestManager.logInfo("Featured languages : " + languages);
        String actualTitle = page.getPageTitle();
        logger.info("Actual page title : {}", actualTitle);
        ExtentTestManager.logInfo("Actual page title : " + actualTitle);
        Assert.assertTrue(actualTitle.toLowerCase().contains(data.expectedTitleFragment().toLowerCase()),
                MSG_TITLE_MISMATCH + " | expected fragment = '" + data.expectedTitleFragment()
                        + "' | actual title = '" + actualTitle + "'");
        logger.info("Test Passed : verifyWikipediaLanguageSelector");
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
