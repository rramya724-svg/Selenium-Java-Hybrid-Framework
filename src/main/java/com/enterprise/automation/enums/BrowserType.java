package com.enterprise.automation.enums;

import com.enterprise.automation.exceptions.FrameworkException;
import java.util.Locale;

public enum BrowserType {
    CHROME, FIREFOX, EDGE;

    public static BrowserType from(String browserName) {
        if (browserName == null || browserName.isBlank()) {
            throw new FrameworkException("Browser name is not configured. Set 'browser' in config.properties.");
        }
        String normalised = browserName.trim().toUpperCase(Locale.ROOT);
        for (BrowserType type : values()) {
            if (type.name().equals(normalised)) {
                return type;
            }
        }
        throw new FrameworkException("Unsupported browser configured: '" + browserName
                + "'. Supported values are CHROME, FIREFOX, EDGE.");
    }
}
