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

public abstract class BasePage {
    protected final Logger logger = LoggerUtility.getLogger(this.getClass());
    protected final WebDriver driver;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        validateDriver();
    }

    protected BasePage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    protected void navigateTo(String url) {
        validateUrl(url);
        logger.info("Navigating to URL [{}] on thread [{}]", url, Thread.currentThread().getName());
        try {
            driver.navigate().to(url);
            logger.info("Navigation command completed for [{}]", url);
        } catch (TimeoutException timeoutException) {
            logger.error(
                    "Browser renderer/page-load timeout while navigating to [{}]. " + "Current URL [{}]",
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

    public void refreshPage() {
        logger.info("Refreshing current page [{}]", getCurrentUrlSafely());
        try {
            driver.navigate().refresh();
            logger.info("Page refresh completed. Current URL [{}]", getCurrentUrlSafely());
        } catch (TimeoutException timeoutException) {
            logger.error("Page refresh timed out. Current URL [{}]", getCurrentUrlSafely(), timeoutException);
            throw timeoutException;
        }
    }

    public String getPageTitle() {
        try {
            String title = driver.getTitle();
            return title == null ? "" : title.trim();
        } catch (RuntimeException exception) {
            logger.error("Unable to retrieve page title", exception);
            throw exception;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected void clickElement(By locator) {
        validateLocator(locator);
        logger.debug("Waiting for element to be clickable: [{}]", locator);
        WebElement element = WaitUtility.waitForClickability(locator);
        try {
            element.click();
            logger.debug("Clicked element [{}] using native click", locator);
        } catch (ElementClickInterceptedException interceptedException) {
            logger.warn("Native click intercepted for [{}]. " + "Using JavaScript click.", locator);
            JavaScriptUtility.click(element);
            logger.debug("Clicked element [{}] using JavaScript", locator);
        }
    }

    protected void typeText(By locator, String text) {
        validateLocator(locator);
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null for locator: " + locator);
        }
        WebElement element = WaitUtility.waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
        logger.debug("Entered text into element [{}]", locator);
    }

    protected String getElementText(By locator) {
        validateLocator(locator);
        return WaitUtility.waitForVisibility(locator).getText().trim();
    }

    protected String getElementAttribute(By locator, String attributeName) {
        validateLocator(locator);
        if (attributeName == null || attributeName.isBlank()) {
            throw new IllegalArgumentException("Attribute name cannot be null or blank");
        }
        return WaitUtility.waitForPresence(locator).getAttribute(attributeName);
    }

    protected boolean isElementDisplayed(By locator, int timeoutSeconds) {
        validateLocator(locator);
        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        return WaitUtility.isElementVisible(locator, timeoutSeconds);
    }

    protected List<String> getAllElementTexts(By locator) {
        validateLocator(locator);
        return WaitUtility.waitForAllVisible(locator).stream().map(WebElement::getText).map(String::trim).toList();
    }

    private void validateDriver() {
        if (this.driver == null) {
            throw new IllegalStateException(
                    "WebDriver is not initialized for thread [" + Thread.currentThread().getName() + "]"
            );
        }
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank");
        }
    }

    private void validateLocator(By locator) {
        if (locator == null) {
            throw new IllegalArgumentException("Locator cannot be null");
        }
    }

    private String getCurrentUrlSafely() {
        try {
            String url = driver.getCurrentUrl();
            return url == null ? "UNKNOWN" : url;
        } catch (RuntimeException exception) {
            return "UNKNOWN";
        }
    }
}
