package com.enterprise.automation.driver;

import com.enterprise.automation.exceptions.FrameworkException;
import org.openqa.selenium.WebDriver;

/**
 * Thread confined holder for the {@link WebDriver} instance of the current thread.
 *
 * <p>Each TestNG worker thread stores its own driver in a {@link ThreadLocal},
 * which is what makes {@code parallel="methods"} safe: no thread can ever see
 * or close another thread's browser. {@link #unload()} is invoked from the
 * {@code finally} block of {@link DriverFactory#quitDriver()} so the thread
 * local entry is always removed, preventing the classic memory leak where a
 * pooled thread keeps a dead driver reference alive for the life of the JVM.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    private DriverManager() {
        throw new IllegalStateException("DriverManager is a utility class and must not be instantiated");
    }

    /**
     * Returns the driver bound to the calling thread.
     *
     * @return the active {@link WebDriver}
     * @throws FrameworkException when no driver has been initialised for this thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver == null) {
            throw new FrameworkException(
                    "No WebDriver is bound to thread '" + Thread.currentThread().getName()
                            + "'. Ensure the test class extends BaseTest so the driver is initialised.");
        }
        return driver;
    }

    /**
     * @return {@code true} when a driver is bound to the calling thread
     */
    public static boolean isDriverInitialised() {
        return DRIVER_THREAD_LOCAL.get() != null;
    }

    /**
     * Binds a driver to the calling thread. Package private by design so that
     * only {@link DriverFactory} can create driver instances.
     *
     * @param driver the driver to bind
     */
    static void setDriver(WebDriver driver) {
        DRIVER_THREAD_LOCAL.set(driver);
    }

    /**
     * Removes the driver reference held by the calling thread.
     */
    static void unload() {
        DRIVER_THREAD_LOCAL.remove();
    }
}
