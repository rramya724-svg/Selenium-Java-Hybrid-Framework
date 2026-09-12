package com.enterprise.automation.enums;

import com.enterprise.automation.exceptions.FrameworkException;

import java.util.Locale;

/**
 * Supported browsers. Keeping the list in an enum removes stringly typed
 * browser handling from {@code DriverFactory} and makes invalid values fail
 * fast with a meaningful message.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public enum BrowserType {

    /** Google Chrome. */
    CHROME,

    /** Mozilla Firefox. */
    FIREFOX,

    /** Microsoft Edge (Chromium). */
    EDGE;

    /**
     * Resolves a configured browser name to an enum constant.
     *
     * @param browserName value read from {@code config.properties} or {@code -Dbrowser}
     * @return the matching {@link BrowserType}
     * @throws FrameworkException when the supplied name is null, blank or unsupported
     */
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
