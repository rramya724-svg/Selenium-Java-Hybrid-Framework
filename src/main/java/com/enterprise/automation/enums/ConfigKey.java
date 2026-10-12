package com.enterprise.automation.enums;

public enum ConfigKey {
    BROWSER("browser"),
    HEADLESS("headless"),
    MAXIMIZE("maximize"),
    WINDOW_WIDTH("window.width"),
    WINDOW_HEIGHT("window.height"),
    IMPLICIT_WAIT("implicit.wait"),
    EXPLICIT_WAIT("explicit.wait"),
    PAGE_LOAD_TIMEOUT("page.load.timeout"),
    SCRIPT_TIMEOUT("script.timeout"),
    POLLING_INTERVAL_MILLIS("polling.interval.millis"),
    ENVIRONMENT("environment"),
    APPLICATION_NAME("application.name"),
    RELEASE_VERSION("release.version"),
    RETRY_COUNT("retry.count"),
    OVERRIDE_REPORTS("override.reports"),
    SCREENSHOT_ON_SUCCESS("screenshot.on.success"),
    SCREENSHOT_ON_FAILURE("screenshot.on.failure"),
    REPORT_PATH("report.path"),
    SCREENSHOT_PATH("screenshot.path"),
    LOG_PATH("log.path"),
    EXCEL_PATH("excel.path"),
    EXCEL_SHEET_NAME("excel.sheet.name"),
    EMAIL_ENABLED("email.enabled"),
    EMAIL_SMTP_HOST("email.smtp.host"),
    EMAIL_SMTP_PORT("email.smtp.port"),
    EMAIL_SMTP_AUTH("email.smtp.auth"),
    EMAIL_SMTP_STARTTLS("email.smtp.starttls"),
    EMAIL_SMTP_SSL("email.smtp.ssl"),
    EMAIL_USERNAME("email.username"),
    EMAIL_PASSWORD("email.password"),
    EMAIL_FROM("email.from"),
    EMAIL_TO("email.to"),
    EMAIL_CC("email.cc"),
    EMAIL_SUBJECT("email.subject"),
    EMAIL_ATTACH_SCREENSHOTS("email.attach.screenshots"),
    EMAIL_START_ENABLED("email.start.enabled"),
    ZEPHYR_ENABLED("zephyr.enabled"),
    ZEPHYR_BASE_URL("zephyr.base.url"),
    ZEPHYR_API_TOKEN("zephyr.api.token"),
    ZEPHYR_PROJECT_KEY("zephyr.project.key"),
    ZEPHYR_TEST_CYCLE_KEY("zephyr.test.cycle.key"),
    ZEPHYR_TEST_CYCLE_NAME_PREFIX("zephyr.test.cycle.name.prefix"),
    ZEPHYR_AUTO_CREATE_TEST_CASES("zephyr.auto.create.test.cases"),
    ZEPHYR_UPDATE_EXISTING_EXECUTION("zephyr.update.existing.execution"),
    ZEPHYR_ENVIRONMENT("zephyr.environment"),
    ZEPHYR_STATUS_PASS("zephyr.status.pass"),
    ZEPHYR_STATUS_FAIL("zephyr.status.fail"),
    ZEPHYR_STATUS_SKIP("zephyr.status.skip"),
    ZEPHYR_STATUS_NOT_EXECUTED("zephyr.status.not.executed"),
    ZEPHYR_MANUAL_TEST_CASES("zephyr.manual.test.cases"),
    ZEPHYR_TIMEOUT_SECONDS("zephyr.timeout.seconds");
    private final String key;

    ConfigKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
