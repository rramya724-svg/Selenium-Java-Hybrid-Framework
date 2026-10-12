package com.enterprise.automation.utilities;

import com.enterprise.automation.constants.FrameworkConstants;
import java.util.Collections;
import java.util.Map;

public record TestDataRow(int rowNumber, Map<String, String> values) {
    public TestDataRow {
        values = Collections.unmodifiableMap(new java.util.LinkedHashMap<>(values));
    }

    public String get(String columnName) {
        return values.getOrDefault(columnName, FrameworkConstants.EMPTY_STRING);
    }

    public String testCaseId() {
        return get(FrameworkConstants.COLUMN_TEST_CASE_ID);
    }

    public String testCaseName() {
        return get(FrameworkConstants.COLUMN_TEST_CASE_NAME);
    }

    public String applicationUrl() {
        return get(FrameworkConstants.COLUMN_APPLICATION_URL);
    }

    public String expectedTitleFragment() {
        return get(FrameworkConstants.COLUMN_EXPECTED_TITLE_FRAGMENT);
    }

    public boolean isExecutable() {
        return FrameworkConstants.EXECUTE_FLAG_YES.equalsIgnoreCase(get(FrameworkConstants.COLUMN_EXECUTE));
    }
}
