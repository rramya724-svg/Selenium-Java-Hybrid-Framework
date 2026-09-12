package com.enterprise.automation.reports;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread safe counters used to build the execution summary block that appears
 * in the console banner and in the notification email.
 *
 * <p>{@link AtomicInteger} is used rather than plain {@code int} because four
 * TestNG worker threads increment these counters concurrently.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
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

    /** Records the suite start time. */
    public static void markSuiteStart() {
        SUITE_START_MILLIS.set(System.currentTimeMillis());
    }

    /** Records the suite end time. */
    public static void markSuiteEnd() {
        SUITE_END_MILLIS.set(System.currentTimeMillis());
    }

    /** Increments the passed counter. */
    public static void incrementPassed() {
        PASSED.incrementAndGet();
    }

    /** Increments the failed counter. */
    public static void incrementFailed() {
        FAILED.incrementAndGet();
    }

    /** Increments the skipped counter. */
    public static void incrementSkipped() {
        SKIPPED.incrementAndGet();
    }

    /** Increments the retry counter. */
    public static void incrementRetried() {
        RETRIED.incrementAndGet();
    }

    /** @return number of passed tests */
    public static int getPassed() {
        return PASSED.get();
    }

    /** @return number of failed tests */
    public static int getFailed() {
        return FAILED.get();
    }

    /** @return number of skipped tests */
    public static int getSkipped() {
        return SKIPPED.get();
    }

    /** @return number of retried attempts */
    public static int getRetried() {
        return RETRIED.get();
    }

    /** @return total number of executed tests */
    public static int getTotal() {
        return PASSED.get() + FAILED.get() + SKIPPED.get();
    }

    /** @return total suite duration in milliseconds */
    public static long getDurationMillis() {
        long end = SUITE_END_MILLIS.get();
        long start = SUITE_START_MILLIS.get();
        return (end > start) ? (end - start) : 0L;
    }

    /** @return the suite start time in epoch milliseconds */
    public static long getStartMillis() {
        return SUITE_START_MILLIS.get();
    }

    /** @return pass percentage rounded to two decimals, zero when nothing ran */
    public static double getPassPercentage() {
        int total = getTotal();
        if (total == 0) {
            return 0.0d;
        }
        return Math.round(((double) PASSED.get() / total) * PERCENTAGE_MULTIPLIER * PERCENTAGE_MULTIPLIER)
                / PERCENTAGE_MULTIPLIER;
    }
}
