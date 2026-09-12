package com.enterprise.automation.utilities;

import com.enterprise.automation.driver.DriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Thin, logged wrapper over the Selenium {@link Actions} API.
 *
 * <p>A new {@link Actions} object is created per call so the helper stays
 * stateless and therefore safe under parallel execution.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class ActionsUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(ActionsUtility.class);

    private ActionsUtility() {
        throw new IllegalStateException("ActionsUtility is a utility class and must not be instantiated");
    }

    private static Actions actions() {
        return new Actions(DriverManager.getDriver());
    }

    /**
     * Moves the mouse pointer over an element.
     *
     * @param element target element
     */
    public static void hoverOver(WebElement element) {
        actions().moveToElement(element).perform();
        LOGGER.debug("Hovered over element");
    }

    /**
     * Performs a double click.
     *
     * @param element target element
     */
    public static void doubleClick(WebElement element) {
        actions().doubleClick(element).perform();
        LOGGER.debug("Double click performed");
    }

    /**
     * Performs a context (right) click.
     *
     * @param element target element
     */
    public static void rightClick(WebElement element) {
        actions().contextClick(element).perform();
        LOGGER.debug("Context click performed");
    }

    /**
     * Drags one element onto another.
     *
     * @param source source element
     * @param target drop target
     */
    public static void dragAndDrop(WebElement source, WebElement target) {
        actions().dragAndDrop(source, target).perform();
        LOGGER.debug("Drag and drop performed");
    }

    /**
     * Types text using low level key events, which some rich editors require.
     *
     * @param element target element
     * @param text    text to type
     */
    public static void sendKeysWithActions(WebElement element, String text) {
        actions().moveToElement(element).click().sendKeys(text).perform();
        LOGGER.debug("Text entered using Actions");
    }

    /**
     * Presses a single keyboard key.
     *
     * @param key key to press
     */
    public static void pressKey(Keys key) {
        actions().sendKeys(key).perform();
        LOGGER.debug("Key [{}] pressed", key);
    }

    /**
     * Clicks while holding the control key, for multi selection scenarios.
     *
     * @param element target element
     */
    public static void controlClick(WebElement element) {
        actions().keyDown(Keys.CONTROL).click(element).keyUp(Keys.CONTROL).perform();
        LOGGER.debug("Control click performed");
    }
}
