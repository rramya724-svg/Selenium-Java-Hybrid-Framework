package com.enterprise.automation.zephyr;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.utilities.LoggerUtility;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public record ZephyrSettings(boolean enabled,
                             String baseUrl,
                             String apiToken,
                             String projectKey,
                             String testCycleKey,
                             String testCycleNamePrefix,
                             boolean autoCreateTestCases,
                             String environmentName,
                             String statusPass,
                             String statusFail,
                             String statusSkip,
                             String statusNotExecuted,
                             List<String> manualTestCaseKeys,
                             Duration timeout) {
    public static final String TOKEN_ENVIRONMENT_VARIABLE = "ZEPHYR_API_TOKEN";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final long REGISTRY_READ_TIMEOUT_SECONDS = 5;
    private static final Logger LOGGER = LoggerUtility.getLogger(ZephyrSettings.class);

    public ZephyrSettings {
        manualTestCaseKeys = List.copyOf(manualTestCaseKeys);
    }

    public static ZephyrSettings load() {
        return new ZephyrSettings(
                ConfigReader.getBoolean(ConfigKey.ZEPHYR_ENABLED, false),
                stripTrailingSlash(ConfigReader.get(ConfigKey.ZEPHYR_BASE_URL, "")),
                resolveToken(),
                ConfigReader.get(ConfigKey.ZEPHYR_PROJECT_KEY, ""),
                ConfigReader.get(ConfigKey.ZEPHYR_TEST_CYCLE_KEY, ""),
                ConfigReader.get(ConfigKey.ZEPHYR_TEST_CYCLE_NAME_PREFIX, "Automation Run"),
                ConfigReader.getBoolean(ConfigKey.ZEPHYR_AUTO_CREATE_TEST_CASES, true),
                ConfigReader.get(ConfigKey.ZEPHYR_ENVIRONMENT, ""),
                ConfigReader.get(ConfigKey.ZEPHYR_STATUS_PASS, "Pass"),
                ConfigReader.get(ConfigKey.ZEPHYR_STATUS_FAIL, "Fail"),
                ConfigReader.get(ConfigKey.ZEPHYR_STATUS_SKIP, "Blocked"),
                ConfigReader.get(ConfigKey.ZEPHYR_STATUS_NOT_EXECUTED, "Not Executed"),
                splitKeys(ConfigReader.get(ConfigKey.ZEPHYR_MANUAL_TEST_CASES, "")),
                Duration.ofSeconds(ConfigReader.getInt(ConfigKey.ZEPHYR_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS)));
    }

    public boolean hasToken() {
        return !apiToken.isBlank();
    }

    public boolean usesFixedTestCycle() {
        return !testCycleKey.isBlank();
    }

    public boolean hasEnvironment() {
        return !environmentName.isBlank();
    }

    @Override
    public String toString() {
        return "ZephyrSettings[enabled=" + enabled + ", baseUrl=" + (baseUrl.isBlank() ? "<auto-detect>" : baseUrl)
                + ", apiToken=" + (hasToken() ? "<set>" : "<missing>") + ", projectKey=" + projectKey
                + ", testCycleKey=" + (usesFixedTestCycle() ? testCycleKey : "<new per run>")
                + ", autoCreateTestCases=" + autoCreateTestCases + ", manualTestCases=" + manualTestCaseKeys
                + ", environmentName=" + environmentName + ", timeout=" + timeout + "]";
    }

    private static String resolveToken() {
        String fromSystemProperty = System.getProperty(ConfigKey.ZEPHYR_API_TOKEN.key());
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty.trim();
        }
        String fromEnvironment = System.getenv(TOKEN_ENVIRONMENT_VARIABLE);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment.trim();
        }
        String fromWindowsUserEnvironment = readWindowsUserEnvironment(TOKEN_ENVIRONMENT_VARIABLE);
        if (!fromWindowsUserEnvironment.isEmpty()) {
            LOGGER.info("Zephyr: {} was not passed to this run, so it was read from your saved Windows user "
                    + "variables (setx). Restarting Eclipse or the terminal makes it available directly.",
                    TOKEN_ENVIRONMENT_VARIABLE);
            return fromWindowsUserEnvironment;
        }
        return ConfigReader.get(ConfigKey.ZEPHYR_API_TOKEN, "");
    }

    static String readWindowsUserEnvironment(String name) {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
            return "";
        }
        try {
            Process process = new ProcessBuilder("reg", "query", "HKCU\\Environment", "/v", name)
                    .redirectErrorStream(true).start();
            String output;
            try (InputStream stdout = process.getInputStream()) {
                output = new String(stdout.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (!process.waitFor(REGISTRY_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "";
            }
            return parseRegQueryValue(output, name);
        } catch (IOException unavailable) {
            return "";
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    static String parseRegQueryValue(String output, String name) {
        for (String line : output.split("\\R")) {
            String[] parts = line.trim().split("\\s+", 3);
            if (parts.length == 3 && parts[0].equalsIgnoreCase(name) && parts[1].startsWith("REG_")) {
                return parts[2].trim();
            }
        }
        return "";
    }

    private static List<String> splitKeys(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(",")).map(String::trim).filter(key -> !key.isEmpty())
                .map(key -> key.toUpperCase(Locale.ROOT)).distinct().toList();
    }

    private static String stripTrailingSlash(String url) {
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
