package com.enterprise.automation.zephyr;

public record ZephyrRunInfo(String suiteName,
                            long startMillis,
                            long endMillis,
                            String browser,
                            boolean headless,
                            String environment) {
    public String browserLabel() {
        return browser + (headless ? " (headless)" : "");
    }
}
