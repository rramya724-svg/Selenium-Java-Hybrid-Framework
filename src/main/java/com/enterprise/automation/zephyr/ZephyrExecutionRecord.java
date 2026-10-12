package com.enterprise.automation.zephyr;

public record ZephyrExecutionRecord(String qualifiedTestName,
                                    String testCaseId,
                                    String testCaseName,
                                    String explicitTestCaseKey,
                                    Outcome outcome,
                                    long startMillis,
                                    long endMillis,
                                    int retryCount,
                                    String failureMessage,
                                    String parameters) {
    public enum Outcome {
        PASSED, FAILED, SKIPPED
    }

    public ZephyrExecutionRecord {
        qualifiedTestName = nullToEmpty(qualifiedTestName);
        testCaseId = nullToEmpty(testCaseId).trim();
        testCaseName = nullToEmpty(testCaseName);
        explicitTestCaseKey = nullToEmpty(explicitTestCaseKey).trim();
        failureMessage = nullToEmpty(failureMessage);
        parameters = nullToEmpty(parameters);
    }

    public boolean hasExplicitTestCaseKey() {
        return !explicitTestCaseKey.isEmpty();
    }

    public long durationMillis() {
        return Math.max(0L, endMillis - startMillis);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
