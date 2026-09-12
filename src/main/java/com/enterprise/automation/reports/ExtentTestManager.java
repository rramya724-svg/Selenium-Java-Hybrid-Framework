package com.enterprise.automation.reports;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.enterprise.automation.utilities.LoggerUtility;
import org.apache.logging.log4j.Logger;

/**
 * Thread confined holder for the {@link ExtentTest} node of the current thread.
 *
 * <p>Without this indirection, parallel tests would write their steps into each
 * other's report nodes. Every helper below is a no-op when no node is bound,
 * so reporting can never be the cause of a test failure.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class ExtentTestManager {

    private static final Logger LOGGER = LoggerUtility.getLogger(ExtentTestManager.class);
    private static final ThreadLocal<ExtentTest> EXTENT_TEST_THREAD_LOCAL = new ThreadLocal<>();

    private ExtentTestManager() {
        throw new IllegalStateException("ExtentTestManager is a utility class and must not be instantiated");
    }

    /**
     * Creates a report node and binds it to the calling thread.
     *
     * @param testName    node title
     * @param description node description
     * @return the created node
     */
    public static ExtentTest startTest(String testName, String description) {
        ExtentTest test = ReportManager.getInstance().createTest(testName, description);
        EXTENT_TEST_THREAD_LOCAL.set(test);
        LOGGER.debug("Extent node created for [{}] on thread [{}]", testName, Thread.currentThread().getName());
        return test;
    }

    /**
     * @return the node bound to the calling thread, or {@code null} when none exists
     */
    public static ExtentTest getTest() {
        return EXTENT_TEST_THREAD_LOCAL.get();
    }

    /**
     * Removes the node reference held by the calling thread.
     */
    public static void unload() {
        EXTENT_TEST_THREAD_LOCAL.remove();
    }

    /**
     * Assigns metadata categories to the current node.
     *
     * @param categories category names
     */
    public static void assignCategories(String... categories) {
        ExtentTest test = getTest();
        if (test != null && categories != null && categories.length > 0) {
            test.assignCategory(categories);
        }
    }

    /**
     * Logs an informational step.
     *
     * @param message step description
     */
    public static void logInfo(String message) {
        log(Status.INFO, message);
    }

    /**
     * Logs a passed step highlighted in green.
     *
     * @param message step description
     */
    public static void logPass(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.PASS, MarkupHelper.createLabel(message, ExtentColor.GREEN));
        }
    }

    /**
     * Logs a failed step highlighted in red.
     *
     * @param message step description
     */
    public static void logFail(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.FAIL, MarkupHelper.createLabel(message, ExtentColor.RED));
        }
    }

    /**
     * Logs a skipped step highlighted in amber.
     *
     * @param message step description
     */
    public static void logSkip(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.SKIP, MarkupHelper.createLabel(message, ExtentColor.ORANGE));
        }
    }

    /**
     * Logs a warning step.
     *
     * @param message step description
     */
    public static void logWarning(String message) {
        log(Status.WARNING, message);
    }

    /**
     * Attaches a throwable and its stack trace to the current node.
     *
     * @param throwable the error to record
     */
    public static void logThrowable(Throwable throwable) {
        ExtentTest test = getTest();
        if (test != null && throwable != null) {
            test.log(Status.FAIL, throwable);
        }
    }

    /**
     * Embeds a Base64 screenshot into the current node.
     *
     * @param status       status under which the image is logged
     * @param base64Image  raw Base64 payload
     * @param title        caption shown above the image
     */
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
