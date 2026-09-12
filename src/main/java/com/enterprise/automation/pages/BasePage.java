package com.enterprise.automation.pages;

import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.utilities.JavaScriptUtility;
import com.enterprise.automation.utilities.LoggerUtility;
import com.enterprise.automation.utilities.WaitUtility;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Superclass of every page object.
 *
 * <p>It holds the driver reference and exposes the small set of synchronised
 * interactions that page objects are allowed to use. Concrete pages therefore
 * describe <em>what</em> the page offers, never <em>how</em> to wait for it.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public abstract class BasePage {

    /** Logger available to every page object. */
    protected final Logger logger = LoggerUtility.getLogger(this.getClass());

    /** Driver instance belonging to the current thread. */
    protected final WebDriver driver;

    /**
     * Binds the page object to the driver of the calling thread.
     */
    protected BasePage() {
        this.driver = DriverManager.getDriver();
    }

    /**
     * Binds the page object to an explicitly supplied driver.
     *
     * @param driver the driver to use
     */
    protected BasePage(WebDriver driver) {
        this.driver = driver;
    }

    /* ------------------------------------------------------------------ */
    /* Navigation                                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Navigates to a URL and waits for the document to finish loading.
     *
     * @param url absolute address to open
     */
    protected void navigateTo(String url) {
        logger.info("Navigating to {}", url);
        driver.get(url);
        WaitUtility.waitForPageLoad();
    }

    /**
     * @return the current page title, never {@code null}
     */
    public String getPageTitle() {
        String title = driver.getTitle();
        return title == null ? "" : title.trim();
    }

    /**
     * @return the current browser URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** Refreshes the current page. */
    public void refreshPage() {
        driver.navigate().refresh();
        WaitUtility.waitForPageLoad();
    }

    /* ------------------------------------------------------------------ */
    /* Synchronised interactions                                           */
    /* ------------------------------------------------------------------ */

    /**
     * Waits for an element to be clickable and clicks it, falling back to a
     * JavaScript click when the native click is intercepted by an overlay.
     *
     * @param locator element locator
     */
    protected void clickElement(By locator) {
        WebElement element = WaitUtility.waitForClickability(locator);
        try {
            element.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException interceptedException) {
            logger.warn("Native click on {} was intercepted; falling back to a JavaScript click", locator);
            JavaScriptUtility.click(element);
        }
        logger.debug("Clicked element {}", locator);
    }

    /**
     * Clears an input and types the supplied text.
     *
     * @param locator element locator
     * @param text    text to enter
     */
    protected void typeText(By locator, String text) {
        WebElement element = WaitUtility.waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
        logger.debug("Entered text into element {}", locator);
    }

    /**
     * Reads the visible text of an element.
     *
     * @param locator element locator
     * @return the trimmed text
     */
    protected String getElementText(By locator) {
        return WaitUtility.waitForVisibility(locator).getText().trim();
    }

    /**
     * Reads an attribute of an element.
     *
     * @param locator       element locator
     * @param attributeName attribute to read
     * @return the attribute value, may be {@code null}
     */
    protected String getElementAttribute(By locator, String attributeName) {
        return WaitUtility.waitForPresence(locator).getAttribute(attributeName);
    }

    /**
     * Checks whether an element is displayed, without throwing when it is absent.
     *
     * @param locator        element locator
     * @param timeoutSeconds how long to wait before giving up
     * @return whether the element became visible
     */
    protected boolean isElementDisplayed(By locator, int timeoutSeconds) {
        return WaitUtility.isElementVisible(locator, timeoutSeconds);
    }

    /**
     * Returns the trimmed text of every element matching the locator.
     *
     * @param locator element locator
     * @return the collected texts in document order
     */
    protected List<String> getAllElementTexts(By locator) {
        return WaitUtility.waitForAllVisible(locator).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }
}
