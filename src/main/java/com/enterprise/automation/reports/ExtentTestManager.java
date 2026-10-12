package com.enterprise.automation.reports;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.enterprise.automation.utilities.LoggerUtility;
import org.apache.logging.log4j.Logger;

public final class ExtentTestManager {
    private static final Logger LOGGER = LoggerUtility.getLogger(ExtentTestManager.class);
    private static final ThreadLocal<ExtentTest> EXTENT_TEST_THREAD_LOCAL = new ThreadLocal<>();

    private ExtentTestManager() {
        throw new IllegalStateException("ExtentTestManager is a utility class and must not be instantiated");
    }

    public static ExtentTest startTest(String testName, String description) {
        ExtentTest test = ReportManager.getInstance().createTest(testName, description);
        EXTENT_TEST_THREAD_LOCAL.set(test);
        LOGGER.debug("Extent node created for [{}] on thread [{}]", testName, Thread.currentThread().getName());
        return test;
    }

    public static ExtentTest getTest() {
        return EXTENT_TEST_THREAD_LOCAL.get();
    }

    public static void unload() {
        EXTENT_TEST_THREAD_LOCAL.remove();
    }

    public static void assignCategories(String... categories) {
        ExtentTest test = getTest();
        if (test != null && categories != null && categories.length > 0) {
            test.assignCategory(categories);
        }
    }

    public static void logInfo(String message) {
        log(Status.INFO, message);
    }

    public static void logPass(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.PASS, MarkupHelper.createLabel(message, ExtentColor.GREEN));
        }
    }

    public static void logFail(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.FAIL, MarkupHelper.createLabel(message, ExtentColor.RED));
        }
    }

    public static void logSkip(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.SKIP, MarkupHelper.createLabel(message, ExtentColor.ORANGE));
        }
    }

    public static void logWarning(String message) {
        log(Status.WARNING, message);
    }

    public static void logThrowable(Throwable throwable) {
        ExtentTest test = getTest();
        if (test != null && throwable != null) {
            test.log(Status.FAIL, throwable);
        }
    }

    public static void attachBase64Screenshot(Status status, String base64Image, String title) {
        ExtentTest test = getTest();
        if (test == null || base64Image == null || base64Image.isBlank()) {
            return;
        }
        test.log(status, title,
                com.aventstack.extentreports.MediaEntityBuilder
                        .createScreenCaptureFromBase64String(base64Image).build());
    }

    private static void log(Status status, String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(status, message);
        }
    }
}
