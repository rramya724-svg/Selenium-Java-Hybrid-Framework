package com.enterprise.automation.utilities;

import com.enterprise.automation.constants.FrameworkConstants;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable value object representing one row of the test data workbook.
 *
 * <p>A record is used deliberately: it is final, its state cannot be mutated by
 * a parallel thread, and it gives correct {@code equals}, {@code hashCode} and
 * {@code toString} implementations without hand written code.</p>
 *
 * @param rowNumber zero based physical row index inside the worksheet, used when writing results back
 * @param values    every column of the row keyed by its header
 * @author Lalith Kumar BV
 * @version 1.0
 */
public record TestDataRow(int rowNumber, Map<String, String> values) {

    /**
     * Canonical constructor that defensively copies the supplied map.
     *
     * @param rowNumber physical row index
     * @param values    column values keyed by header
     */
    public TestDataRow {
        values = Collections.unmodifiableMap(new java.util.LinkedHashMap<>(values));
    }

    /**
     * Reads a column value.
     *
     * @param columnName the header name
     * @return the trimmed value, or an empty string when the column is absent
     */
    public String get(String columnName) {
        return values.getOrDefault(columnName, FrameworkConstants.EMPTY_STRING);
    }

    /** @return the unique test case identifier */
    public String testCaseId() {
        return get(FrameworkConstants.COLUMN_TEST_CASE_ID);
    }

    /** @return the human readable test case name */
    public String testCaseName() {
        return get(FrameworkConstants.COLUMN_TEST_CASE_NAME);
    }

    /** @return the application under test URL */
    public String applicationUrl() {
        return get(FrameworkConstants.COLUMN_APPLICATION_URL);
    }

    /** @return the expected page title fragment */
    public String expectedTitleFragment() {
        return get(FrameworkConstants.COLUMN_EXPECTED_TITLE_FRAGMENT);
    }

    /** @return {@code true} when the Execute flag is set to YES */
    public boolean isExecutable() {
        return FrameworkConstants.EXECUTE_FLAG_YES.equalsIgnoreCase(get(FrameworkConstants.COLUMN_EXECUTE));
    }
}
