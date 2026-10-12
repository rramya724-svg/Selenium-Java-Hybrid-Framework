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
        properties.stringPropertyNames().forEach(name -> resolved.put(name, properties.getProperty(name).trim()));
        return Collections.unmodifiableMap(resolved);
    }

    public static String get(ConfigKey configKey) {
        String value = resolve(configKey);
        if (value == null || value.isBlank()) {
            throw new FrameworkException("Mandatory configuration key '" + configKey.key()
                    + "' is missing or blank in config.properties");
        }
        return value;
    }

    public static String get(ConfigKey configKey, String defaultValue) {
        String value = resolve(configKey);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

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

    public static boolean getBoolean(ConfigKey configKey, boolean defaultValue) {
        String value = resolve(configKey);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
    }

    public static Map<String, String> asMap() {
        return CONFIGURATION;
    }

    private static String resolve(ConfigKey configKey) {
        String systemOverride = System.getProperty(configKey.key());
        return (systemOverride != null && !systemOverride.isBlank()) ? systemOverride.trim()
                : CONFIGURATION.get(configKey.key());
    }
}
