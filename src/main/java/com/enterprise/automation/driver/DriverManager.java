package com.enterprise.automation.driver;

import com.enterprise.automation.exceptions.FrameworkException;
import org.openqa.selenium.WebDriver;

public final class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    private DriverManager() {
        throw new IllegalStateException("DriverManager is a utility class and must not be instantiated");
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver == null) {
            throw new FrameworkException(
                    "No WebDriver is bound to thread '" + Thread.currentThread().getName()
                            + "'. Ensure the test class extends BaseTest so the driver is initialised.");
        }
        return driver;
    }

    public static boolean isDriverInitialised() {
        return DRIVER_THREAD_LOCAL.get() != null;
    }

    static void setDriver(WebDriver driver) {
        DRIVER_THREAD_LOCAL.set(driver);
    }

    static void unload() {
        DRIVER_THREAD_LOCAL.remove();
    }
}
