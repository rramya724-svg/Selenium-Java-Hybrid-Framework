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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Creates, configures and disposes {@link WebDriver} instances.
 *
 * <p>This is the only class in the framework permitted to instantiate a driver.
 * It applies the configured timeouts, headless flags and window geometry, then
 * hands the instance to {@link DriverManager} for thread confinement.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class DriverFactory {

    private static final Logger LOGGER = LoggerUtility.getLogger(DriverFactory.class);

    private static final String ARG_HEADLESS_CHROMIUM = "--headless=new";
    private static final String ARG_HEADLESS_FIREFOX = "-headless";
    private static final String ARG_DISABLE_NOTIFICATIONS = "--disable-notifications";
    private static final String ARG_DISABLE_DEV_SHM = "--disable-dev-shm-usage";
    private static final String ARG_NO_SANDBOX = "--no-sandbox";
    private static final String ARG_REMOTE_ALLOW_ORIGINS = "--remote-allow-origins=*";
    private static final String ARG_DISABLE_GPU = "--disable-gpu";
    private static final String ARG_INCOGNITO = "--incognito";

    private DriverFactory() {
        throw new IllegalStateException("DriverFactory is a utility class and must not be instantiated");
    }

    /**
     * Initialises a browser for the calling thread using the configured browser name.
     *
     * @return the freshly created {@link WebDriver}
     */
    public static WebDriver initialiseDriver() {
        return initialiseDriver(BrowserType.from(ConfigReader.get(ConfigKey.BROWSER)));
    }

    /**
     * Initialises the requested browser for the calling thread.
     *
     * @param browserType the browser to launch
     * @return the freshly created {@link WebDriver}
     * @throws FrameworkException when the browser cannot be started
     */
    public static WebDriver initialiseDriver(BrowserType browserType) {
        if (DriverManager.isDriverInitialised()) {
            LOGGER.warn("A driver is already bound to thread [{}]; quitting it before creating a new one",
                    Thread.currentThread().getName());
            quitDriver();
        }

        boolean headless = ConfigReader.getBoolean(ConfigKey.HEADLESS, false);
        LOGGER.info("Initialising [{}] driver on thread [{}] | headless = {}",
                browserType, Thread.currentThread().getName(), headless);

        WebDriver driver;
        try {
            driver = createDriver(browserType, headless);
        } catch (RuntimeException runtimeException) {
            throw new FrameworkException("Failed to initialise the " + browserType + " driver", runtimeException);
        }

        applyTimeouts(driver);
        applyWindowState(driver, headless);

        DriverManager.setDriver(driver);
        LOGGER.info("Driver initialised successfully on thread [{}]", Thread.currentThread().getName());
        return driver;
    }

    private static WebDriver createDriver(BrowserType browserType, boolean headless) {
        return switch (browserType) {
            case CHROME -> {
                WebDriverManager.chromedriver().setup();
                yield new ChromeDriver(buildChromeOptions(headless));
            }
            case FIREFOX -> {
                WebDriverManager.firefoxdriver().setup();
                yield new FirefoxDriver(buildFirefoxOptions(headless));
            }
            case EDGE -> {
                WebDriverManager.edgedriver().setup();
                yield new EdgeDriver(buildEdgeOptions(headless));
            }
        };
    }

    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(ARG_DISABLE_NOTIFICATIONS, ARG_DISABLE_DEV_SHM,
                ARG_NO_SANDBOX, ARG_REMOTE_ALLOW_ORIGINS, ARG_INCOGNITO);
        if (headless) {
            options.addArguments(ARG_HEADLESS_CHROMIUM, ARG_DISABLE_GPU, windowSizeArgument());
        }
        return options;
    }

    private static FirefoxOptions buildFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("dom.webnotifications.enabled", false);
        if (headless) {
            options.addArguments(ARG_HEADLESS_FIREFOX);
        }
        return options;
    }

    private static EdgeOptions buildEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments(ARG_DISABLE_NOTIFICATIONS, ARG_DISABLE_DEV_SHM,
                ARG_NO_SANDBOX, ARG_REMOTE_ALLOW_ORIGINS);
        if (headless) {
            options.addArguments(ARG_HEADLESS_CHROMIUM, ARG_DISABLE_GPU, windowSizeArgument());
        }
        return options;
    }

    private static String windowSizeArgument() {
        return "--window-size="
                + ConfigReader.getInt(ConfigKey.WINDOW_WIDTH, FrameworkConstants.DEFAULT_WINDOW_WIDTH)
                + ","
                + ConfigReader.getInt(ConfigKey.WINDOW_HEIGHT, FrameworkConstants.DEFAULT_WINDOW_HEIGHT);
    }

    private static void applyTimeouts(WebDriver driver) {
        int implicitWait = ConfigReader.getInt(ConfigKey.IMPLICIT_WAIT,
                FrameworkConstants.DEFAULT_IMPLICIT_WAIT_SECONDS);
        int pageLoadTimeout = ConfigReader.getInt(ConfigKey.PAGE_LOAD_TIMEOUT,
                FrameworkConstants.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
        int scriptTimeout = ConfigReader.getInt(ConfigKey.SCRIPT_TIMEOUT,
                FrameworkConstants.DEFAULT_SCRIPT_TIMEOUT_SECONDS);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(scriptTimeout));

        LOGGER.debug("Timeouts applied | implicit = {}s, pageLoad = {}s, script = {}s",
                implicitWait, pageLoadTimeout, scriptTimeout);
    }

    private static void applyWindowState(WebDriver driver, boolean headless) {
        if (headless) {
            driver.manage().window().setSize(new Dimension(
                    ConfigReader.getInt(ConfigKey.WINDOW_WIDTH, FrameworkConstants.DEFAULT_WINDOW_WIDTH),
                    ConfigReader.getInt(ConfigKey.WINDOW_HEIGHT, FrameworkConstants.DEFAULT_WINDOW_HEIGHT)));
            LOGGER.debug("Headless viewport applied");
            return;
        }
        if (ConfigReader.getBoolean(ConfigKey.MAXIMIZE, true)) {
            driver.manage().window().maximize();
            LOGGER.debug("Browser window maximised");
        }
    }

    /**
     * Quits the driver bound to the calling thread and clears the thread local.
     *
     * <p>The thread local is cleared in a {@code finally} block so a failure
     * inside {@code quit()} can never leak a driver reference.</p>
     */
    public static void quitDriver() {
        if (!DriverManager.isDriverInitialised()) {
            LOGGER.debug("No driver bound to thread [{}]; nothing to quit", Thread.currentThread().getName());
            return;
        }
        try {
            DriverManager.getDriver().quit();
            LOGGER.info("Driver quit successfully on thread [{}]", Thread.currentThread().getName());
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Driver did not quit cleanly on thread [{}]",
                    Thread.currentThread().getName(), runtimeException);
        } finally {
            DriverManager.unload();
        }
    }
}
