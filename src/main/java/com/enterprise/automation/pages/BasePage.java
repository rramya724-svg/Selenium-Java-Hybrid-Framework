package com.enterprise.automation.pages;

import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.utilities.JavaScriptUtility;
import com.enterprise.automation.utilities.LoggerUtility;
import com.enterprise.automation.utilities.WaitUtility;

import org.apache.logging.log4j.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Superclass of every page object.
 *
 * <p>
 * Provides common browser navigation and synchronized element
 * interactions for all page objects.
 * </p>
 *
 * <p>
 * Navigation is designed for both local execution and Jenkins/AWS
 * CI execution. Browser configuration such as page-load strategy
 * remains the responsibility of DriverFactory.
 * </p>
 *
 * @author Lalith Kumar BV
 * @version 2.0
 */
public abstract class BasePage {

    /**
     * Logger available to every page object.
     */
    protected final Logger logger =
            LoggerUtility.getLogger(this.getClass());

    /**
     * Driver belonging to the current execution thread.
     */
    protected final WebDriver driver;

    /**
     * Creates a page object using the driver associated with
     * the current TestNG thread.
     */
    protected BasePage() {

        this.driver =
                DriverManager.getDriver();

        validateDriver();
    }

    /**
     * Creates a page object using an explicitly supplied driver.
     *
     * @param driver WebDriver instance
     */
    protected BasePage(WebDriver driver) {

        if (driver == null) {
            throw new IllegalArgumentException(
                    "WebDriver cannot be null"
            );
        }

        this.driver = driver;
    }

    /* ============================================================= */
    /* Navigation                                                     */
    /* ============================================================= */

    /**
     * Navigates to the supplied URL.
     *
     * <p>
     * DriverFactory controls the Selenium page-load strategy.
     * This method therefore does not blindly wait for the entire
     * page a second time.
     * </p>
     *
     * <p>
     * A renderer timeout is logged with useful diagnostic information.
     * The exception is rethrown because silently continuing after a
     * failed navigation can produce misleading test results.
     * </p>
     *
     * @param url absolute URL
     */
    protected void navigateTo(String url) {

        validateUrl(url);

        logger.info(
                "Navigating to URL [{}] on thread [{}]",
                url,
                Thread.currentThread().getName()
        );

        try {

            driver.navigate().to(url);

            logger.info(
                    "Navigation command completed for [{}]",
                    url
            );

        } catch (TimeoutException timeoutException) {

            logger.error(
                    "Browser renderer/page-load timeout while navigating to [{}]. "
                            + "Current URL [{}]",
                    url,
                    getCurrentUrlSafely(),
                    timeoutException
            );

            throw timeoutException;

        } catch (RuntimeException runtimeException) {

            logger.error(
                    "Navigation failed for URL [{}]. Current URL [{}]",
                    url,
                    getCurrentUrlSafely(),
                    runtimeException
            );

            throw runtimeException;
        }
    }

    /**
     * Refreshes the current page.
     *
     * <p>
     * Uses the configured page-load strategy from DriverFactory.
     * </p>
     */
    public void refreshPage() {

        logger.info(
                "Refreshing current page [{}]",
                getCurrentUrlSafely()
        );

        try {

            driver.navigate().refresh();

            logger.info(
                    "Page refresh completed. Current URL [{}]",
                    getCurrentUrlSafely()
            );

        } catch (TimeoutException timeoutException) {

            logger.error(
                    "Page refresh timed out. Current URL [{}]",
                    getCurrentUrlSafely(),
                    timeoutException
            );

            throw timeoutException;
        }
    }

    /**
     * Returns the current page title.
     *
     * @return trimmed title or empty string
     */
    public String getPageTitle() {

        try {

            String title =
                    driver.getTitle();

            return title == null
                    ? ""
                    : title.trim();

        } catch (RuntimeException exception) {

            logger.error(
                    "Unable to retrieve page title",
                    exception
            );

            throw exception;
        }
    }

    /**
     * Returns the current browser URL.
     *
     * @return current URL
     */
    public String getCurrentUrl() {

        return driver.getCurrentUrl();
    }

    /* ============================================================= */
    /* Click                                                          */
    /* ============================================================= */

    /**
     * Waits for an element to become clickable and clicks it.
     *
     * <p>
     * Falls back to JavaScript click only when the native click
     * is intercepted.
     * </p>
     *
     * @param locator element locator
     */
    protected void clickElement(By locator) {

        validateLocator(locator);

        logger.debug(
                "Waiting for element to be clickable: [{}]",
                locator
        );

        WebElement element =
                WaitUtility.waitForClickability(locator);

        try {

            element.click();

            logger.debug(
                    "Clicked element [{}] using native click",
                    locator
            );

        } catch (ElementClickInterceptedException interceptedException) {

            logger.warn(
                    "Native click intercepted for [{}]. "
                            + "Using JavaScript click.",
                    locator
            );

            JavaScriptUtility.click(element);

            logger.debug(
                    "Clicked element [{}] using JavaScript",
                    locator
            );
        }
    }

    /* ============================================================= */
    /* Text input                                                     */
    /* ============================================================= */

    /**
     * Clears an input field and enters text.
     *
     * @param locator element locator
     * @param text text to enter
     */
    protected void typeText(
            By locator,
            String text) {

        validateLocator(locator);

        if (text == null) {
            throw new IllegalArgumentException(
                    "Text cannot be null for locator: "
                            + locator
            );
        }

        WebElement element =
                WaitUtility.waitForVisibility(locator);

        element.clear();
        element.sendKeys(text);

        logger.debug(
                "Entered text into element [{}]",
                locator
        );
    }

    /* ============================================================= */
    /* Element text                                                   */
    /* ============================================================= */

    /**
     * Gets visible text from an element.
     *
     * @param locator element locator
     * @return trimmed element text
     */
    protected String getElementText(By locator) {

        validateLocator(locator);

        return WaitUtility
                .waitForVisibility(locator)
                .getText()
                .trim();
    }

    /**
     * Gets an element attribute.
     *
     * @param locator element locator
     * @param attributeName attribute name
     * @return attribute value
     */
    protected String getElementAttribute(
            By locator,
            String attributeName) {

        validateLocator(locator);

        if (attributeName == null ||
                attributeName.isBlank()) {

            throw new IllegalArgumentException(
                    "Attribute name cannot be null or blank"
            );
        }

        return WaitUtility
                .waitForPresence(locator)
                .getAttribute(attributeName);
    }

    /* ============================================================= */
    /* Visibility                                                      */
    /* ============================================================= */

    /**
     * Determines whether an element becomes visible
     * within the supplied timeout.
     *
     * @param locator element locator
     * @param timeoutSeconds timeout in seconds
     * @return true when visible, otherwise false
     */
    protected boolean isElementDisplayed(
            By locator,
            int timeoutSeconds) {

        validateLocator(locator);

        if (timeoutSeconds < 0) {

            throw new IllegalArgumentException(
                    "Timeout cannot be negative"
            );
        }

        return WaitUtility.isElementVisible(
                locator,
                timeoutSeconds
        );
    }

    /* ============================================================= */
    /* Multiple elements                                               */
    /* ============================================================= */

    /**
     * Returns trimmed text for all visible matching elements.
     *
     * @param locator element locator
     * @return texts in document order
     */
    protected List<String> getAllElementTexts(
            By locator) {

        validateLocator(locator);

        return WaitUtility
                .waitForAllVisible(locator)
                .stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }

    /* ============================================================= */
    /* Validation / diagnostics                                        */
    /* ============================================================= */

    /**
     * Validates that the current thread has a WebDriver.
     */
    private void validateDriver() {

        if (this.driver == null) {

            throw new IllegalStateException(
                    "WebDriver is not initialized for thread ["
                            + Thread.currentThread().getName()
                            + "]"
            );
        }
    }

    /**
     * Validates URL input.
     */
    private void validateUrl(String url) {

        if (url == null ||
                url.isBlank()) {

            throw new IllegalArgumentException(
                    "URL cannot be null or blank"
            );
        }
    }

    /**
     * Validates locator input.
     */
    private void validateLocator(By locator) {

        if (locator == null) {

            throw new IllegalArgumentException(
                    "Locator cannot be null"
            );
        }
    }

    /**
     * Safely retrieves the current URL for diagnostics.
     *
     * @return current URL or UNKNOWN when unavailable
     */
    private String getCurrentUrlSafely() {

        try {

            String url =
                    driver.getCurrentUrl();

            return url == null
                    ? "UNKNOWN"
                    : url;

        } catch (RuntimeException exception) {

            return "UNKNOWN";
        }
    }
}