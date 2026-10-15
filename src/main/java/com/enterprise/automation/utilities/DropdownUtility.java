package com.enterprise.automation.utilities;

import com.enterprise.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import java.util.List;

public final class DropdownUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(DropdownUtility.class);

    private DropdownUtility() {
        throw new IllegalStateException("DropdownUtility is a utility class and must not be instantiated");
    }

    private static Select toSelect(WebElement element) {
        try {
            return new Select(element);
        } catch (UnexpectedTagNameException unexpectedTagNameException) {
            throw new FrameworkException(
                    "The supplied element is not a native <select> element", unexpectedTagNameException);
        }
    }

    public static void selectByVisibleText(WebElement element, String visibleText) {
        toSelect(element).selectByVisibleText(visibleText);
        LOGGER.debug("Dropdown option selected by visible text [{}]", visibleText);
    }

    public static void selectByValue(WebElement element, String value) {
        toSelect(element).selectByValue(value);
        LOGGER.debug("Dropdown option selected by value [{}]", value);
    }

    public static void selectByIndex(WebElement element, int index) {
        toSelect(element).selectByIndex(index);
        LOGGER.debug("Dropdown option selected by index [{}]", index);
    }

    public static List<String> getAllOptions(WebElement element) {
        return toSelect(element).getOptions().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }

    public static String getSelectedOption(WebElement element) {
        return toSelect(element).getFirstSelectedOption().getText().trim();
    }

    public static boolean isMultiSelect(WebElement element) {
        return toSelect(element).isMultiple();
    }

    public static void deselectAll(WebElement element) {
        toSelect(element).deselectAll();
        LOGGER.debug("All dropdown selections cleared");
    }
}
