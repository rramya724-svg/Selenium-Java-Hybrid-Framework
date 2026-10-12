package com.enterprise.automation.constants;

import java.io.File;

public final class FrameworkConstants {
    private FrameworkConstants() {
        throw new IllegalStateException("FrameworkConstants is a utility class and must not be instantiated");
    }

    public static final String USER_DIRECTORY = System.getProperty("user.dir");
    public static final String FILE_SEPARATOR = File.separator;
    public static final String RESOURCES_DIRECTORY =
            USER_DIRECTORY + FILE_SEPARATOR + "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "resources";
    public static final String CONFIG_FILE_PATH = RESOURCES_DIRECTORY + FILE_SEPARATOR + "config.properties";
    public static final String OUTPUT_DIRECTORY = USER_DIRECTORY + FILE_SEPARATOR + "test-output";
    public static final String SUREFIRE_REPORT_DIRECTORY = OUTPUT_DIRECTORY + FILE_SEPARATOR + "surefire-reports";
    public static final String EXTENT_REPORT_FILE_NAME = "ExtentReport.html";
    public static final String EMAILABLE_REPORT_FILE_NAME = "emailable-report.html";
    public static final String TESTNG_INDEX_REPORT_FILE_NAME = "index.html";
    public static final String EXECUTION_LOG_FILE_NAME = "execution.log";
    public static final String SCREENSHOT_EXTENSION = ".png";
    public static final int DEFAULT_IMPLICIT_WAIT_SECONDS = 10;
    public static final int DEFAULT_EXPLICIT_WAIT_SECONDS = 20;
    public static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 45;
    public static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 30;
    public static final long DEFAULT_POLLING_INTERVAL_MILLIS = 500L;
    public static final int DEFAULT_WINDOW_WIDTH = 1920;
    public static final int DEFAULT_WINDOW_HEIGHT = 1080;
    public static final int DEFAULT_RETRY_COUNT = 2;
    public static final String COLUMN_TEST_CASE_ID = "TestCaseId";
    public static final String COLUMN_TEST_CASE_NAME = "TestCaseName";
    public static final String COLUMN_APPLICATION_URL = "ApplicationUrl";
    public static final String COLUMN_EXPECTED_TITLE_FRAGMENT = "ExpectedTitleFragment";
    public static final String COLUMN_EXECUTE = "Execute";
    public static final String COLUMN_STATUS = "Status";
    public static final String COLUMN_EXECUTION_TIMESTAMP = "ExecutionTimestamp";
    public static final String COLUMN_EXECUTION_TIME_MS = "ExecutionTimeMs";
    public static final String COLUMN_SCREENSHOT_PATH = "ScreenshotPath";
    public static final String EXECUTE_FLAG_YES = "YES";
    public static final String STATUS_PASS = "PASS";
    public static final String STATUS_FAIL = "FAIL";
    public static final String STATUS_SKIP = "SKIP";
    public static final String DISPLAY_TIMESTAMP_PATTERN = "dd-MMM-yyyy HH:mm:ss";
    public static final String FILE_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss_SSS";
    public static final String EMPTY_STRING = "";
}
