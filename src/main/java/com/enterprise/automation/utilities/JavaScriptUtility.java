package com.enterprise.automation.utilities;

import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * Wrapper around {@link JavascriptExecutor} for the operations that the plain
 * WebDriver API cannot perform reliably, such as scrolling a lazily rendered
 * element into view or clicking an element covered by a sticky header.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class JavaScriptUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(JavaScriptUtility.class);

    private static final String SCROLL_INTO_VIEW = "arguments[0].scrollIntoView({block:'center', inline:'center'});";
    private static final String CLICK_ELEMENT = "arguments[0].click();";
    private static final String SET_VALUE = "arguments[0].value = arguments[1];";
    private static final String HIGHLIGHT = "arguments[0].style.border = '3px solid red';";
    private static final String SCROLL_TO_BOTTOM = "window.scrollTo(0, document.body.scrollHeight);";
    private static final String SCROLL_TO_TOP = "window.scrollTo(0, 0);";
    private static final String PAGE_TITLE = "return document.title;";
    private static final String INNER_TEXT = "return arguments[0].innerText;";

    private JavaScriptUtility() {
        throw new IllegalStateException("JavaScriptUtility is a utility class and must not be instantiated");
    }

    private static JavascriptExecutor executor() {
        if (DriverManager.getDriver() instanceof JavascriptExecutor javascriptExecutor) {
            return javascriptExecutor;
        }
        throw new FrameworkException("The active WebDriver does not support JavaScript execution");
    }

    /**
     * Scrolls the supplied element to the centre of the viewport.
     *
     * @param element target element
     */
    public static void scrollIntoView(WebElement element) {
        executor().executeScript(SCROLL_INTO_VIEW, element);
        LOGGER.debug("Scrolled element into view");
    }

    /**
     * Clicks an element through JavaScript, bypassing overlay interception.
     *
     * @param element target element
     */
    public static void click(WebElement element) {
        executor().executeScript(CLICK_ELEMENT, element);
        LOGGER.debug("JavaScript click performed");
    }

    /**
     * Sets the value of an input element directly.
     *
     * @param element target element
     * @param value   value to assign
     */
    public static void setValue(WebElement element, String value) {
        executor().executeScript(SET_VALUE, element, value);
        LOGGER.debug("JavaScript value assignment performed");
    }

    /**
     * Draws a red border around an element, useful when debugging locally.
     *
     * @param element target element
     */
    public static void highlight(WebElement element) {
        executor().executeScript(HIGHLIGHT, element);
    }

    /** Scrolls to the bottom of the page. */
    public static void scrollToBottom() {
        executor().executeScript(SCROLL_TO_BOTTOM);
    }

    /** Scrolls to the top of the page. */
    public static void scrollToTop() {
        executor().executeScript(SCROLL_TO_TOP);
    }

    /**
     * @return the page title read through the DOM rather than the WebDriver API
     */
    public static String getPageTitle() {
        return String.valueOf(executor().executeScript(PAGE_TITLE));
    }

    /**
     * Reads {@code innerText} of an element, which returns rendered text even
     * when {@code getText()} returns an empty string.
     *
     * @param element target element
     * @return the rendered text
     */
    public static String getInnerText(WebElement element) {
        return String.valueOf(executor().executeScript(INNER_TEXT, element));
    }
}
