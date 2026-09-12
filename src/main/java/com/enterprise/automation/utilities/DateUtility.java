package com.enterprise.automation.utilities;

import com.enterprise.automation.constants.FrameworkConstants;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and duration formatting helpers shared by reports, screenshots and Excel.
 *
 * <p>{@link DateTimeFormatter} is immutable and thread safe, so the two shared
 * instances below are safe for parallel execution.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class DateUtility {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern(FrameworkConstants.DISPLAY_TIMESTAMP_PATTERN);

    private static final DateTimeFormatter FILE_FORMATTER =
            DateTimeFormatter.ofPattern(FrameworkConstants.FILE_TIMESTAMP_PATTERN);

    private static final long MILLIS_PER_SECOND = 1000L;
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long MINUTES_PER_HOUR = 60L;

    private DateUtility() {
        throw new IllegalStateException("DateUtility is a utility class and must not be instantiated");
    }

    /**
     * @return the current timestamp formatted for human consumption
     */
    public static String currentDisplayTimestamp() {
        return LocalDateTime.now().format(DISPLAY_FORMATTER);
    }

    /**
     * @return the current timestamp formatted for use inside file names
     */
    public static String currentFileTimestamp() {
        return LocalDateTime.now().format(FILE_FORMATTER);
    }

    /**
     * Converts an epoch millisecond value into a display timestamp.
     *
     * @param epochMillis milliseconds since the epoch
     * @return formatted timestamp
     */
    public static String toDisplayTimestamp(long epochMillis) {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMillis),
                java.time.ZoneId.systemDefault()).format(DISPLAY_FORMATTER);
    }

    /**
     * Renders a duration as {@code HHh MMm SSs MMMms}, trimming empty leading units.
     *
     * @param millis duration in milliseconds
     * @return a compact human readable duration
     */
    public static String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % MINUTES_PER_HOUR;
        long seconds = duration.toSeconds() % SECONDS_PER_MINUTE;
        long remainingMillis = millis % MILLIS_PER_SECOND;

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (hours > 0 || minutes > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s ").append(remainingMillis).append("ms");
        return builder.toString();
    }
}
