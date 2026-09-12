package com.enterprise.automation.config;

import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Immutable, thread safe accessor for {@code config.properties}.
 *
 * <p>The file is read exactly once inside a static initialiser and copied into
 * an unmodifiable map, so concurrent reads from parallel TestNG threads need no
 * synchronisation at all. A JVM system property of the same name always wins,
 * which is what allows {@code mvn test -Dbrowser=edge -Dheadless=true} to work
 * without editing any file.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class ConfigReader {

    private static final Map<String, String> CONFIGURATION = loadConfiguration();

    private ConfigReader() {
        throw new IllegalStateException("ConfigReader is a utility class and must not be instantiated");
    }

    private static Map<String, String> loadConfiguration() {
        Properties properties = new Properties();
        Path configPath = Path.of(FrameworkConstants.CONFIG_FILE_PATH);

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        } catch (IOException ioException) {
            throw new FrameworkException(
                    "Unable to load configuration file from: " + FrameworkConstants.CONFIG_FILE_PATH, ioException);
        }

        Map<String, String> resolved = new HashMap<>();
        properties.stringPropertyNames()
                  .forEach(name -> resolved.put(name, properties.getProperty(name).trim()));
        return Collections.unmodifiableMap(resolved);
    }

    /**
     * Returns a mandatory configuration value.
     *
     * @param configKey the key to resolve
     * @return the configured value, never {@code null} or blank
     * @throws FrameworkException when the key is absent or blank
     */
    public static String get(ConfigKey configKey) {
        String value = resolve(configKey);
        if (value == null || value.isBlank()) {
            throw new FrameworkException("Mandatory configuration key '" + configKey.key()
                    + "' is missing or blank in config.properties");
        }
        return value;
    }

    /**
     * Returns an optional configuration value.
     *
     * @param configKey    the key to resolve
     * @param defaultValue value returned when the key is absent or blank
     * @return the configured value or {@code defaultValue}
     */
    public static String get(ConfigKey configKey, String defaultValue) {
        String value = resolve(configKey);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /**
     * Returns a numeric configuration value.
     *
     * @param configKey    the key to resolve
     * @param defaultValue value returned when the key is absent or not numeric
     * @return the resolved integer
     */
    public static int getInt(ConfigKey configKey, int defaultValue) {
        String value = resolve(configKey);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException numberFormatException) {
            throw new FrameworkException("Configuration key '" + configKey.key()
                    + "' must be a whole number but was '" + value + "'", numberFormatException);
        }
    }

    /**
     * Returns a numeric configuration value as a long.
     *
     * @param configKey    the key to resolve
     * @param defaultValue value returned when the key is absent
     * @return the resolved long
     */
    public static long getLong(ConfigKey configKey, long defaultValue) {
        String value = resolve(configKey);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException numberFormatException) {
            throw new FrameworkException("Configuration key '" + configKey.key()
                    + "' must be a whole number but was '" + value + "'", numberFormatException);
        }
    }

    /**
     * Returns a boolean configuration value.
     *
     * @param configKey    the key to resolve
     * @param defaultValue value returned when the key is absent
     * @return {@code true} only when the configured value equals "true", ignoring case
     */
    public static boolean getBoolean(ConfigKey configKey, boolean defaultValue) {
        String value = resolve(configKey);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
    }

    /**
     * Exposes the full, read only configuration snapshot, primarily for report headers.
     *
     * @return an unmodifiable view of every configured key and value
     */
    public static Map<String, String> asMap() {
        return CONFIGURATION;
    }

    private static String resolve(ConfigKey configKey) {
        String systemOverride = System.getProperty(configKey.key());
        return (systemOverride != null && !systemOverride.isBlank())
                ? systemOverride.trim()
                : CONFIGURATION.get(configKey.key());
    }
}
