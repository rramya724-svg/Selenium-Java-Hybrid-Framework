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
 * Test Case 2:
 * Launch browser, open Flipkart, validate the home page,
 * validate search availability and verify the page title.
 *
 * @author Lalith Kumar BV
 * @version 2.0
 */
public class FlipkartHomePageTest extends BaseTest {

    private static final String TEST_CASE_ID = "TC_002";

    private static final String GROUP_SMOKE = "smoke";
    private static final String GROUP_REGRESSION = "regression";
    private static final String GROUP_E_COMMERCE = "ecommerce";

    private static final String MSG_PAGE_NOT_LOADED =
            "The Flipkart home page did not render its brand logo";

    private static final String MSG_SEARCH_BOX_MISSING =
            "The Flipkart product search box was not displayed on the home page";

    private static final String MSG_TITLE_MISMATCH =
            "The Flipkart home page title did not contain the expected fragment";

    /**
     * Validates that the Flipkart home page loads,
     * exposes the search box and contains the expected title.
     */
    @Test(
            description = "Verify that the Flipkart home page loads and exposes the expected title",
            groups = {
                    GROUP_SMOKE,
                    GROUP_REGRESSION,
                    GROUP_E_COMMERCE
            },
            priority = 2
    )
    public void verifyFlipkartHomePageTitle() {

        TestDataRow data =
                resolveTestData();

        markTestCaseId(TEST_CASE_ID);

        logger.info(
                "============================================================"
        );

        logger.info(
                "Test Started : verifyFlipkartHomePageTitle"
        );

        logger.info(
                "Test Case ID : {}",
                TEST_CASE_ID
        );

        logger.info(
                "Application URL : {}",
                data.applicationUrl()
        );

        logger.info(
                "Expected title fragment : {}",
                data.expectedTitleFragment()
        );

        ExtentTestManager.logInfo(
                "Opening " + data.applicationUrl()
        );

        /*
         * Open application.
         */
        FlipkartHomePage flipkartHomePage =
                new FlipkartHomePage()
                        .openHomePage(data.applicationUrl());

        /*
         * Flipkart may display a login popup.
         * Its absence is also a valid state.
         */
        boolean popupDismissed =
                flipkartHomePage.dismissLoginPopupIfPresent();

        logger.info(
                "Login popup dismissed: {}",
                popupDismissed
        );

        /*
         * Validate that the actual page rendered.
         */
        boolean homePageLoaded =
                flipkartHomePage.isHomePageLoaded();

        Assert.assertTrue(
                homePageLoaded,
                MSG_PAGE_NOT_LOADED
        );

        ExtentTestManager.logInfo(
                "Flipkart brand logo rendered successfully"
        );

        /*
         * Validate search box.
         */
        boolean searchBoxDisplayed =
                flipkartHomePage.isSearchBoxDisplayed();

        Assert.assertTrue(
                searchBoxDisplayed,
                MSG_SEARCH_BOX_MISSING
        );

        ExtentTestManager.logInfo(
                "Flipkart search box is available"
        );

        /*
         * Validate page title.
         */
        String actualTitle =
                flipkartHomePage.getPageTitle();

        String expectedTitleFragment =
                data.expectedTitleFragment();

        logger.info(
                "Actual page title : [{}]",
                actualTitle
        );

        logger.info(
                "Expected title fragment : [{}]",
                expectedTitleFragment
        );

        ExtentTestManager.logInfo(
                "Actual page title : " + actualTitle
        );

        Assert.assertNotNull(
                actualTitle,
                "Flipkart page title must not be null"
        );

        Assert.assertFalse(
                actualTitle.isBlank(),
                "Flipkart page title must not be blank"
        );

        Assert.assertTrue(
                actualTitle
                        .toLowerCase()
                        .contains(
                                expectedTitleFragment
                                        .toLowerCase()
                        ),
                MSG_TITLE_MISMATCH
                        + " | expected fragment = '"
                        + expectedTitleFragment
                        + "' | actual title = '"
                        + actualTitle
                        + "'"
        );

        ExtentTestManager.logInfo(
                "Expected title fragment was found successfully"
        );

        logger.info(
                "Test Passed : verifyFlipkartHomePageTitle"
        );

        logger.info(
                "============================================================"
        );
    }

    /**
     * Retrieves test data for this test case.
     *
     * @return test data
     */
    private TestDataRow resolveTestData() {

        return ExcelUtility
                .findByTestCaseId(TEST_CASE_ID)
                .orElseThrow(
                        () -> new com.enterprise.automation.exceptions.FrameworkException(
                                "Test data row ["
                                        + TEST_CASE_ID
                                        + "] is missing from testdata.xlsx"
                        )
                );
    }

    /**
     * Stores the test case ID for listeners/reporting.
     *
     * @param testCaseId test case ID
     */
    private void markTestCaseId(
            String testCaseId) {

        ITestResult currentResult =
                Reporter.getCurrentTestResult();

        if (currentResult != null) {

            currentResult.setAttribute(
                    TEST_CASE_ID_ATTRIBUTE,
                    testCaseId
            );
        }
    }
}