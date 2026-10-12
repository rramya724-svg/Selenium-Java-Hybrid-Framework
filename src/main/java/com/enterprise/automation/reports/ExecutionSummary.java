package com.enterprise.automation.reports;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class ExecutionSummary {
    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();
    private static final AtomicInteger SKIPPED = new AtomicInteger();
    private static final AtomicInteger RETRIED = new AtomicInteger();
    private static final AtomicLong SUITE_START_MILLIS = new AtomicLong();
    private static final AtomicLong SUITE_END_MILLIS = new AtomicLong();
    private static final double PERCENTAGE_MULTIPLIER = 100.0d;

    private ExecutionSummary() {
        throw new IllegalStateException("ExecutionSummary is a utility class and must not be instantiated");
    }

    public static void markSuiteStart() {
        SUITE_START_MILLIS.set(System.currentTimeMillis());
    }

    public static void markSuiteEnd() {
        SUITE_END_MILLIS.set(System.currentTimeMillis());
    }

    public static void incrementPassed() {
        PASSED.incrementAndGet();
    }

    public static void incrementFailed() {
        FAILED.incrementAndGet();
    }

    public static void incrementSkipped() {
        SKIPPED.incrementAndGet();
    }

    public static void incrementRetried() {
        RETRIED.incrementAndGet();
    }

    public static int getPassed() {
        return PASSED.get();
    }

    public static int getFailed() {
        return FAILED.get();
    }

    public static int getSkipped() {
        return SKIPPED.get();
    }

    public static int getRetried() {
        return RETRIED.get();
    }

    public static int getTotal() {
        return PASSED.get() + FAILED.get() + SKIPPED.get();
    }

    public static long getDurationMillis() {
        long end = SUITE_END_MILLIS.get();
        long start = SUITE_START_MILLIS.get();
        return (end > start) ? (end - start) : 0L;
    }

    public static long getStartMillis() {
        return SUITE_START_MILLIS.get();
    }

    public static double getPassPercentage() {
        int total = getTotal();
        if (total == 0) {
            return 0.0d;
        }
        return Math.round(((double) PASSED.get() / total) * PERCENTAGE_MULTIPLIER * PERCENTAGE_MULTIPLIER)
                / PERCENTAGE_MULTIPLIER;
    }
}
