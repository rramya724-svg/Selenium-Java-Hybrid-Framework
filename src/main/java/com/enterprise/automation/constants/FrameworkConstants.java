package com.enterprise.automation.constants;

import java.io.File;

/**
 * Central, immutable holder for every framework level constant.
 *
 * <p>No magic numbers or duplicated literals are allowed anywhere else in the
 * framework; everything that is fixed at build time lives here, and everything
 * that changes per environment lives in {@code config.properties}.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class FrameworkConstants {

    private FrameworkConstants() {
        throw new IllegalStateException("FrameworkConstants is a utility class and must not be instantiated");
    }

    /* ------------------------------------------------------------------ */
    /* Directory layout                                                    */
    /* ------------------------------------------------------------------ */

    /** Project root directory resolved at runtime. */
    public static final String USER_DIRECTORY = System.getProperty("user.dir");

    /** Platform independent file separator. */
    public static final String FILE_SEPARATOR = File.separator;

    /** Absolute path of {@code src/test/resources}. */
    public static final String RESOURCES_DIRECTORY =
            USER_DIRECTORY + FILE_SEPARATOR + "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "resources";

    /** Absolute path of {@code config.properties}. */
    public static final String CONFIG_FILE_PATH = RESOURCES_DIRECTORY + FILE_SEPARATOR + "config.properties";

    /** Absolute path of the framework output directory. */
    public static final String OUTPUT_DIRECTORY = USER_DIRECTORY + FILE_SEPARATOR + "test-output";

    /** Absolute path of the Surefire report directory. */
    public static final String SUREFIRE_REPORT_DIRECTORY = OUTPUT_DIRECTORY + FILE_SEPARATOR + "surefire-reports";

    /** Name of the generated Extent report file. */
    public static final String EXTENT_REPORT_FILE_NAME = "ExtentReport.html";

    /** Name of the TestNG generated emailable report. */
    public static final String EMAILABLE_REPORT_FILE_NAME = "emailable-report.html";

    /** Name of the TestNG generated index report. */
    public static final String TESTNG_INDEX_REPORT_FILE_NAME = "index.html";

    /** Name of the rolling execution log file. */
    public static final String EXECUTION_LOG_FILE_NAME = "execution.log";

    /** Extension used for every captured screenshot. */
    public static final String SCREENSHOT_EXTENSION = ".png";

    /* ------------------------------------------------------------------ */
    /* Timeout defaults (used only when config.properties omits a value)    */
    /* ------------------------------------------------------------------ */

    /** Default implicit wait in seconds. */
    public static final int DEFAULT_IMPLICIT_WAIT_SECONDS = 10;

    /** Default explicit wait in seconds. */
    public static final int DEFAULT_EXPLICIT_WAIT_SECONDS = 20;

    /** Default page load timeout in seconds. */
    public static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 45;

    /** Default asynchronous script timeout in seconds. */
    public static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 30;

    /** Default polling interval, in milliseconds, for fluent waits. */
    public static final long DEFAULT_POLLING_INTERVAL_MILLIS = 500L;

    /** Default browser viewport width used in headless mode. */
    public static final int DEFAULT_WINDOW_WIDTH = 1920;

    /** Default browser viewport height used in headless mode. */
    public static final int DEFAULT_WINDOW_HEIGHT = 1080;

    /* ------------------------------------------------------------------ */
    /* Retry                                                               */
    /* ------------------------------------------------------------------ */

    /** Default number of retries applied to a failed test. */
    public static final int DEFAULT_RETRY_COUNT = 2;

    /* ------------------------------------------------------------------ */
    /* Excel column headers                                                */
    /* ------------------------------------------------------------------ */

    /** Excel column holding the unique test case identifier. */
    public static final String COLUMN_TEST_CASE_ID = "TestCaseId";

    /** Excel column holding the human readable test case name. */
    public static final String COLUMN_TEST_CASE_NAME = "TestCaseName";

    /** Excel column holding the application under test URL. */
    public static final String COLUMN_APPLICATION_URL = "ApplicationUrl";

    /** Excel column holding the expected page title fragment. */
    public static final String COLUMN_EXPECTED_TITLE_FRAGMENT = "ExpectedTitleFragment";

    /** Excel column that switches a data row on or off. */
    public static final String COLUMN_EXECUTE = "Execute";

    /** Excel column into which PASS / FAIL / SKIP is written back. */
    public static final String COLUMN_STATUS = "Status";

    /** Excel column into which the execution timestamp is written back. */
    public static final String COLUMN_EXECUTION_TIMESTAMP = "ExecutionTimestamp";

    /** Excel column into which the execution duration is written back. */
    public static final String COLUMN_EXECUTION_TIME_MS = "ExecutionTimeMs";

    /** Excel column into which the screenshot path is written back. */
    public static final String COLUMN_SCREENSHOT_PATH = "ScreenshotPath";

    /** Value that enables a data row. */
    public static final String EXECUTE_FLAG_YES = "YES";

    /* ------------------------------------------------------------------ */
    /* Result literals                                                     */
    /* ------------------------------------------------------------------ */

    /** Passed status literal. */
    public static final String STATUS_PASS = "PASS";

    /** Failed status literal. */
    public static final String STATUS_FAIL = "FAIL";

    /** Skipped status literal. */
    public static final String STATUS_SKIP = "SKIP";

    /* ------------------------------------------------------------------ */
    /* Formatting                                                          */
    /* ------------------------------------------------------------------ */

    /** Human readable timestamp pattern used in reports and Excel. */
    public static final String DISPLAY_TIMESTAMP_PATTERN = "dd-MMM-yyyy HH:mm:ss";

    /** File system safe timestamp pattern used in artefact names. */
    public static final String FILE_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss_SSS";

    /** Empty string constant, prevents duplicated literals. */
    public static final String EMPTY_STRING = "";
}
