package com.enterprise.automation.driver;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.enums.BrowserType;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.exceptions.FrameworkException;
import com.enterprise.automation.utilities.LoggerUtility;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.logging.log4j.Logger;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Factory responsible for creating, configuring and disposing WebDriver
 * instances.
 *
 * <p>
 * This class is the only location in the framework where WebDriver
 * instances are created.
 * </p>
 *
 * <p>
 * The implementation is designed for:
 * </p>
 * <ul>
 *     <li>Local execution</li>
 *     <li>Jenkins CI execution</li>
 *     <li>AWS Linux execution</li>
 *     <li>Headless Chrome execution</li>
 *     <li>TestNG parallel execution</li>
 *     <li>Safe driver cleanup</li>
 * </ul>
 *
 * @author Lalith Kumar BV
 * @version 2.0
 */
public final class DriverFactory {

    private static final Logger LOGGER =
            LoggerUtility.getLogger(DriverFactory.class);

    /*
     * Chrome arguments.
     */
    private static final String ARG_HEADLESS_CHROMIUM =
            "--headless=new";

    private static final String ARG_HEADLESS_FIREFOX =
            "-headless";

    private static final String ARG_DISABLE_NOTIFICATIONS =
            "--disable-notifications";

    private static final String ARG_DISABLE_DEV_SHM =
            "--disable-dev-shm-usage";

    private static final String ARG_NO_SANDBOX =
            "--no-sandbox";

    private static final String ARG_REMOTE_ALLOW_ORIGINS =
            "--remote-allow-origins=*";

    private static final String ARG_DISABLE_GPU =
            "--disable-gpu";

    private static final String ARG_INCOGNITO =
            "--incognito";

    private static final String ARG_DISABLE_EXTENSIONS =
            "--disable-extensions";

    private static final String ARG_DISABLE_POPUP_BLOCKING =
            "--disable-popup-blocking";

    private static final String ARG_DISABLE_BACKGROUND_NETWORKING =
            "--disable-background-networking";

    private static final String ARG_DISABLE_COMPONENT_UPDATE =
            "--disable-component-update";

    private static final String ARG_DISABLE_DEFAULT_APPS =
            "--disable-default-apps";

    private static final String ARG_NO_FIRST_RUN =
            "--no-first-run";

    private static final String ARG_NO_DEFAULT_BROWSER_CHECK =
            "--no-default-browser-check";

    private static final String ARG_WINDOW_SIZE =
            "--window-size=";

    private DriverFactory() {
        throw new IllegalStateException(
                "DriverFactory is a utility class and must not be instantiated"
        );
    }

    /**
     * Initializes the browser configured in config.properties.
     *
     * @return initialized WebDriver
     */
    public static WebDriver initialiseDriver() {

        BrowserType browserType =
                BrowserType.from(
                        ConfigReader.get(ConfigKey.BROWSER)
                );

        return initialiseDriver(browserType);
    }

    /**
     * Initializes the requested browser for the current thread.
     *
     * @param browserType browser to initialize
     * @return initialized WebDriver
     */
    public static WebDriver initialiseDriver(
            BrowserType browserType) {

        if (browserType == null) {
            throw new FrameworkException(
                    "Browser type cannot be null"
            );
        }

        /*
         * Protect against an old driver remaining on the same
         * TestNG worker thread.
         */
        if (DriverManager.isDriverInitialised()) {

            LOGGER.warn(
                    "Existing WebDriver detected on thread [{}]. "
                            + "Closing it before creating a new driver.",
                    Thread.currentThread().getName()
            );

            quitDriver();
        }

        boolean headless =
                ConfigReader.getBoolean(
                        ConfigKey.HEADLESS,
                        false
                );

        LOGGER.info(
                "Initializing [{}] browser | thread = [{}] | headless = [{}]",
                browserType,
                Thread.currentThread().getName(),
                headless
        );

        WebDriver driver = null;

        try {

            driver =
                    createDriver(
                            browserType,
                            headless
                    );

            if (driver == null) {
                throw new FrameworkException(
                        "WebDriver creation returned null for browser: "
                                + browserType
                );
            }

            applyTimeouts(driver);

            applyWindowState(
                    driver,
                    headless
            );

            /*
             * Store the driver only after the complete configuration
             * has succeeded.
             */
            DriverManager.setDriver(driver);

            LOGGER.info(
                    "WebDriver initialized successfully | browser = [{}] | thread = [{}]",
                    browserType,
                    Thread.currentThread().getName()
            );

            return driver;

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Failed to initialize [{}] browser on thread [{}]",
                    browserType,
                    Thread.currentThread().getName(),
                    exception
            );

            /*
             * If driver creation succeeded but later configuration
             * failed, close the partially initialized browser.
             */
            if (driver != null) {
                try {
                    driver.quit();
                } catch (RuntimeException quitException) {
                    LOGGER.warn(
                            "Failed to close partially initialized browser",
                            quitException
                    );
                }
            }

            DriverManager.unload();

            throw new FrameworkException(
                    "Failed to initialize the "
                            + browserType
                            + " driver",
                    exception
            );
        }
    }

    /**
     * Creates the requested WebDriver.
     */
    private static WebDriver createDriver(
            BrowserType browserType,
            boolean headless) {

        return switch (browserType) {

            case CHROME -> {

                WebDriverManager
                        .chromedriver()
                        .setup();

                yield new ChromeDriver(
                        buildChromeOptions(headless)
                );
            }

            case FIREFOX -> {

                WebDriverManager
                        .firefoxdriver()
                        .setup();

                yield new FirefoxDriver(
                        buildFirefoxOptions(headless)
                );
            }

            case EDGE -> {

                WebDriverManager
                        .edgedriver()
                        .setup();

                yield new EdgeDriver(
                        buildEdgeOptions(headless)
                );
            }
        };
    }

    /**
     * Builds Chrome options suitable for Jenkins/AWS Linux execution.
     *
     * <p>
     * EAGER page loading is intentionally used for CI execution.
     * This prevents the test from unnecessarily waiting for every
     * background resource before Selenium continues.
     * </p>
     */
    private static ChromeOptions buildChromeOptions(
            boolean headless) {

        ChromeOptions options =
                new ChromeOptions();

        /*
         * CI stability options.
         */
        options.addArguments(
                ARG_DISABLE_NOTIFICATIONS,
                ARG_DISABLE_DEV_SHM,
                ARG_NO_SANDBOX,
                ARG_REMOTE_ALLOW_ORIGINS,
                ARG_DISABLE_EXTENSIONS,
                ARG_DISABLE_POPUP_BLOCKING,
                ARG_DISABLE_BACKGROUND_NETWORKING,
                ARG_DISABLE_COMPONENT_UPDATE,
                ARG_DISABLE_DEFAULT_APPS,
                ARG_NO_FIRST_RUN,
                ARG_NO_DEFAULT_BROWSER_CHECK,
                ARG_INCOGNITO
        );

        /*
         * IMPORTANT:
         *
         * EAGER allows Selenium to continue once the DOM is ready
         * instead of waiting for every page resource.
         *
         * This is particularly useful for CI tests against pages
         * containing heavy third-party resources.
         */
        options.setPageLoadStrategy(
                PageLoadStrategy.EAGER
        );

        if (headless) {

            options.addArguments(
                    ARG_HEADLESS_CHROMIUM,
                    ARG_DISABLE_GPU,
                    windowSizeArgument()
            );

            LOGGER.info(
                    "Chrome configured for headless CI execution"
            );
        }

        return options;
    }

    /**
     * Builds Firefox options.
     */
    private static FirefoxOptions buildFirefoxOptions(
            boolean headless) {

        FirefoxOptions options =
                new FirefoxOptions();

        options.addPreference(
                "dom.webnotifications.enabled",
                false
        );

        options.setPageLoadStrategy(
                PageLoadStrategy.EAGER
        );

        if (headless) {
            options.addArguments(
                    ARG_HEADLESS_FIREFOX
            );
        }

        return options;
    }

    /**
     * Builds Edge options.
     */
    private static EdgeOptions buildEdgeOptions(
            boolean headless) {

        EdgeOptions options =
                new EdgeOptions();

        options.addArguments(
                ARG_DISABLE_NOTIFICATIONS,
                ARG_DISABLE_DEV_SHM,
                ARG_NO_SANDBOX,
                ARG_REMOTE_ALLOW_ORIGINS,
                ARG_DISABLE_EXTENSIONS,
                ARG_DISABLE_POPUP_BLOCKING,
                ARG_NO_FIRST_RUN,
                ARG_NO_DEFAULT_BROWSER_CHECK
        );

        options.setPageLoadStrategy(
                PageLoadStrategy.EAGER
        );

        if (headless) {

            options.addArguments(
                    ARG_HEADLESS_CHROMIUM,
                    ARG_DISABLE_GPU,
                    windowSizeArgument()
            );
        }

        return options;
    }

    /**
     * Returns configured browser viewport size.
     */
    private static String windowSizeArgument() {

        int width =
                ConfigReader.getInt(
                        ConfigKey.WINDOW_WIDTH,
                        FrameworkConstants.DEFAULT_WINDOW_WIDTH
                );

        int height =
                ConfigReader.getInt(
                        ConfigKey.WINDOW_HEIGHT,
                        FrameworkConstants.DEFAULT_WINDOW_HEIGHT
                );

        return ARG_WINDOW_SIZE
                + width
                + ","
                + height;
    }

    /**
     * Applies Selenium timeout configuration.
     */
    private static void applyTimeouts(
            WebDriver driver) {

        int implicitWait =
                ConfigReader.getInt(
                        ConfigKey.IMPLICIT_WAIT,
                        FrameworkConstants.DEFAULT_IMPLICIT_WAIT_SECONDS
                );

        int pageLoadTimeout =
                ConfigReader.getInt(
                        ConfigKey.PAGE_LOAD_TIMEOUT,
                        FrameworkConstants.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS
                );

        int scriptTimeout =
                ConfigReader.getInt(
                        ConfigKey.SCRIPT_TIMEOUT,
                        FrameworkConstants.DEFAULT_SCRIPT_TIMEOUT_SECONDS
                );

        driver.manage()
                .timeouts()
                .implicitlyWait(
                        Duration.ofSeconds(
                                Math.max(0, implicitWait)
                        )
                );

        driver.manage()
                .timeouts()
                .pageLoadTimeout(
                        Duration.ofSeconds(
                                Math.max(1, pageLoadTimeout)
                        )
                );

        driver.manage()
                .timeouts()
                .scriptTimeout(
                        Duration.ofSeconds(
                                Math.max(1, scriptTimeout)
                        )
                );

        LOGGER.info(
                "Timeouts configured | implicit={}s | pageLoad={}s | script={}s",
                implicitWait,
                pageLoadTimeout,
                scriptTimeout
        );
    }

    /**
     * Applies browser window configuration.
     */
    private static void applyWindowState(
            WebDriver driver,
            boolean headless) {

        int width =
                ConfigReader.getInt(
                        ConfigKey.WINDOW_WIDTH,
                        FrameworkConstants.DEFAULT_WINDOW_WIDTH
                );

        int height =
                ConfigReader.getInt(
                        ConfigKey.WINDOW_HEIGHT,
                        FrameworkConstants.DEFAULT_WINDOW_HEIGHT
                );

        /*
         * Explicitly set the viewport for headless execution.
         */
        if (headless) {

            driver.manage()
                    .window()
                    .setSize(
                            new Dimension(
                                    width,
                                    height
                            )
                    );

            LOGGER.debug(
                    "Headless browser viewport configured as {}x{}",
                    width,
                    height
            );

            return;
        }

        /*
         * For normal desktop execution, maximize when configured.
         */
        if (ConfigReader.getBoolean(
                ConfigKey.MAXIMIZE,
                true
        )) {

            driver.manage()
                    .window()
                    .maximize();

            LOGGER.debug(
                    "Browser window maximized"
            );
        }
    }

    /**
     * Quits and removes the WebDriver associated with the current thread.
     *
     * <p>
     * The ThreadLocal is always cleared, even if ChromeDriver.quit()
     * throws an exception.
     * </p>
     */
    public static void quitDriver() {

        if (!DriverManager.isDriverInitialised()) {

            LOGGER.debug(
                    "No WebDriver bound to thread [{}]",
                    Thread.currentThread().getName()
            );

            return;
        }

        WebDriver driver =
                DriverManager.getDriver();

        try {

            if (driver != null) {

                LOGGER.info(
                        "Closing WebDriver on thread [{}]",
                        Thread.currentThread().getName()
                );

                driver.quit();

                LOGGER.info(
                        "WebDriver closed successfully on thread [{}]",
                        Thread.currentThread().getName()
                );
            }

        } catch (RuntimeException exception) {

            /*
             * Cleanup failure should not prevent ThreadLocal
             * cleanup.
             */
            LOGGER.error(
                    "WebDriver did not close cleanly on thread [{}]",
                    Thread.currentThread().getName(),
                    exception
            );

        } finally {

            DriverManager.unload();

            LOGGER.debug(
                    "Driver ThreadLocal cleared on thread [{}]",
                    Thread.currentThread().getName()
            );
        }
    }
}