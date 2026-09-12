package com.enterprise.automation.enums;

/**
 * Type safe representation of every key present in {@code config.properties}.
 *
 * <p>Using an enum instead of raw strings eliminates typo driven runtime
 * failures and removes duplicated string literals flagged by SonarQube.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public enum ConfigKey {

    /** Browser to launch. */
    BROWSER("browser"),
    /** Headless execution switch. */
    HEADLESS("headless"),
    /** Window maximise switch. */
    MAXIMIZE("maximize"),
    /** Viewport width used in headless mode. */
    WINDOW_WIDTH("window.width"),
    /** Viewport height used in headless mode. */
    WINDOW_HEIGHT("window.height"),

    /** Implicit wait in seconds. */
    IMPLICIT_WAIT("implicit.wait"),
    /** Explicit wait in seconds. */
    EXPLICIT_WAIT("explicit.wait"),
    /** Page load timeout in seconds. */
    PAGE_LOAD_TIMEOUT("page.load.timeout"),
    /** Asynchronous script timeout in seconds. */
    SCRIPT_TIMEOUT("script.timeout"),
    /** Fluent wait polling interval in milliseconds. */
    POLLING_INTERVAL_MILLIS("polling.interval.millis"),

    /** Logical environment name. */
    ENVIRONMENT("environment"),
    /** Application name shown in reports. */
    APPLICATION_NAME("application.name"),
    /** Release version shown in reports. */
    RELEASE_VERSION("release.version"),

    /** Retry count applied to failed tests. */
    RETRY_COUNT("retry.count"),
    /** Whether previous reports are overwritten. */
    OVERRIDE_REPORTS("override.reports"),
    /** Capture screenshot for passed tests. */
    SCREENSHOT_ON_SUCCESS("screenshot.on.success"),
    /** Capture screenshot for failed tests. */
    SCREENSHOT_ON_FAILURE("screenshot.on.failure"),

    /** Relative Extent report directory. */
    REPORT_PATH("report.path"),
    /** Relative screenshot directory. */
    SCREENSHOT_PATH("screenshot.path"),
    /** Relative log directory. */
    LOG_PATH("log.path"),
    /** Relative path of the test data workbook. */
    EXCEL_PATH("excel.path"),
    /** Worksheet name holding the test data. */
    EXCEL_SHEET_NAME("excel.sheet.name"),

    /** Master switch for the email utility. */
    EMAIL_ENABLED("email.enabled"),
    /** SMTP host. */
    EMAIL_SMTP_HOST("email.smtp.host"),
    /** SMTP port. */
    EMAIL_SMTP_PORT("email.smtp.port"),
    /** SMTP authentication switch. */
    EMAIL_SMTP_AUTH("email.smtp.auth"),
    /** STARTTLS switch. */
    EMAIL_SMTP_STARTTLS("email.smtp.starttls"),
    /** Implicit SSL switch. */
    EMAIL_SMTP_SSL("email.smtp.ssl"),
    /** SMTP user name. */
    EMAIL_USERNAME("email.username"),
    /** SMTP password or application password. */
    EMAIL_PASSWORD("email.password"),
    /** From address. */
    EMAIL_FROM("email.from"),
    /** Comma separated To addresses. */
    EMAIL_TO("email.to"),
    /** Comma separated Cc addresses. */
    EMAIL_CC("email.cc"),
    /** Subject line prefix. */
    EMAIL_SUBJECT("email.subject"),
    /** Whether screenshots are attached to the email. */
    EMAIL_ATTACH_SCREENSHOTS("email.attach.screenshots");

    private final String key;

    ConfigKey(String key) {
        this.key = key;
    }

    /**
     * @return the literal key as written in {@code config.properties}
     */
    public String key() {
        return key;
    }
}
