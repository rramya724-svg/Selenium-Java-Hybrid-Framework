package com.enterprise.automation.utilities;

import com.enterprise.automation.driver.DriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public final class ActionsUtility {
    private static final Logger LOGGER = LoggerUtility.getLogger(ActionsUtility.class);

    private ActionsUtility() {
        throw new IllegalStateException("ActionsUtility is a utility class and must not be instantiated");
    }

    private static Actions actions() {
        return new Actions(DriverManager.getDriver());
    }

    public static void hoverOver(WebElement element) {
        actions().moveToElement(element).perform();
        LOGGER.debug("Hovered over element");
    }

    public static void doubleClick(WebElement element) {
        actions().doubleClick(element).perform();
        LOGGER.debug("Double click performed");
    }

    public static void rightClick(WebElement element) {
        actions().contextClick(element).perform();
        LOGGER.debug("Context click performed");
    }

    public static void dragAndDrop(WebElement source, WebElement target) {
        actions().dragAndDrop(source, target).perform();
        LOGGER.debug("Drag and drop performed");
    }

    public static void sendKeysWithActions(WebElement element, String text) {
        actions().moveToElement(element).click().sendKeys(text).perform();
        LOGGER.debug("Text entered using Actions");
    }

    public static void pressKey(Keys key) {
        actions().sendKeys(key).perform();
        LOGGER.debug("Key [{}] pressed", key);
    }

    public static void controlClick(WebElement element) {
        actions().keyDown(Keys.CONTROL).click(element).keyUp(Keys.CONTROL).perform();
        LOGGER.debug("Control click performed");
    }
}
