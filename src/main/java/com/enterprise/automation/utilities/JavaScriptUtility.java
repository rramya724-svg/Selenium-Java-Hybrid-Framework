package com.enterprise.automation.utilities;

import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

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

    public static void scrollIntoView(WebElement element) {
        executor().executeScript(SCROLL_INTO_VIEW, element);
        LOGGER.debug("Scrolled element into view");
    }

    public static void click(WebElement element) {
        executor().executeScript(CLICK_ELEMENT, element);
        LOGGER.debug("JavaScript click performed");
    }

    public static void setValue(WebElement element, String value) {
        executor().executeScript(SET_VALUE, element, value);
        LOGGER.debug("JavaScript value assignment performed");
    }

    public static void highlight(WebElement element) {
        executor().executeScript(HIGHLIGHT, element);
    }

    public static void scrollToBottom() {
        executor().executeScript(SCROLL_TO_BOTTOM);
    }

    public static void scrollToTop() {
        executor().executeScript(SCROLL_TO_TOP);
    }

    public static String getPageTitle() {
        return String.valueOf(executor().executeScript(PAGE_TITLE));
    }

    public static String getInnerText(WebElement element) {
        return String.valueOf(executor().executeScript(INNER_TEXT, element));
    }
}
