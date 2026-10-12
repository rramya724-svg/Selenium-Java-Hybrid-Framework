package com.enterprise.automation.utilities;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public final class WaitUtility {
    private static final Logger LOGGER = LoggerUtility.getLogger(WaitUtility.class);
    private static final String DOCUMENT_READY_SCRIPT = "return document.readyState";
    private static final String READY_STATE_COMPLETE = "complete";

    private WaitUtility() {
        throw new IllegalStateException("WaitUtility is a utility class and must not be instantiated");
    }

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    private static Duration defaultTimeout() {
        return Duration.ofSeconds(
                ConfigReader.getInt(ConfigKey.EXPLICIT_WAIT, FrameworkConstants.DEFAULT_EXPLICIT_WAIT_SECONDS));
    }

    private static Duration pollingInterval() {
        return Duration.ofMillis(
                ConfigReader.getLong(ConfigKey.POLLING_INTERVAL_MILLIS,
                        FrameworkConstants.DEFAULT_POLLING_INTERVAL_MILLIS));
    }

    public static WebDriverWait explicitWait() {
        return new WebDriverWait(driver(), defaultTimeout(), pollingInterval());
    }

    public static WebDriverWait explicitWait(int timeoutSeconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(timeoutSeconds), pollingInterval());
    }

    public static FluentWait<WebDriver> fluentWait(int timeoutSeconds) {
        return new FluentWait<>(driver()).withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(pollingInterval()).ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    public static WebElement waitForVisibility(By locator) {
        return await(ExpectedConditions.visibilityOfElementLocated(locator),
                "visibility of element located by " + locator);
    }

    public static WebElement waitForClickability(By locator) {
        return await(ExpectedConditions.elementToBeClickable(locator), "clickability of element located by " + locator);
    }

    public static WebElement waitForPresence(By locator) {
        return await(ExpectedConditions.presenceOfElementLocated(locator), "presence of element located by " + locator);
    }

    public static List<WebElement> waitForAllVisible(By locator) {
        return await(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator),
                "visibility of all elements located by " + locator);
    }

    public static boolean waitForInvisibility(By locator) {
        return await(ExpectedConditions.invisibilityOfElementLocated(locator),
                "invisibility of element located by " + locator);
    }

    public static boolean waitForTitleContains(String titleFragment) {
        return await(ExpectedConditions.titleContains(titleFragment), "page title to contain '" + titleFragment + "'");
    }

    public static boolean waitForUrlContains(String urlFragment) {
        return await(ExpectedConditions.urlContains(urlFragment), "current URL to contain '" + urlFragment + "'");
    }

    public static void waitForPageLoad() {
        ExpectedCondition<Boolean> pageLoaded = webDriver -> {
            if (webDriver instanceof JavascriptExecutor javascriptExecutor) {
                return READY_STATE_COMPLETE.equals(
                        String.valueOf(javascriptExecutor.executeScript(DOCUMENT_READY_SCRIPT)));
            }
            return Boolean.TRUE;
        };
        await(pageLoaded, "document.readyState to become 'complete'");
        LOGGER.debug("Page load completed on thread [{}]", Thread.currentThread().getName());
    }

    public static boolean isElementVisible(By locator, int timeoutSeconds) {
        try {
            explicitWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException timeoutException) {
            LOGGER.debug("Element {} was not visible within {}s", locator, timeoutSeconds);
            return false;
        }
    }

    private static <T> T await(ExpectedCondition<T> condition, String description) {
        try {
            return explicitWait().until(condition);
        } catch (TimeoutException timeoutException) {
            throw new FrameworkException("Timed out after " + defaultTimeout().toSeconds()
                    + "s waiting for " + description, timeoutException);
        }
    }
}
