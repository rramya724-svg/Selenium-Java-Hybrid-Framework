package com.enterprise.automation.utilities;

import com.enterprise.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import java.util.List;

/**
 * Helpers for native HTML {@code <select>} elements.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
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

    /**
     * Selects an option by its visible text.
     *
     * @param element     the select element
     * @param visibleText option text
     */
    public static void selectByVisibleText(WebElement element, String visibleText) {
        toSelect(element).selectByVisibleText(visibleText);
        LOGGER.debug("Dropdown option selected by visible text [{}]", visibleText);
    }

    /**
     * Selects an option by its {@code value} attribute.
     *
     * @param element the select element
     * @param value   option value
     */
    public static void selectByValue(WebElement element, String value) {
        toSelect(element).selectByValue(value);
        LOGGER.debug("Dropdown option selected by value [{}]", value);
    }

    /**
     * Selects an option by its zero based index.
     *
     * @param element the select element
     * @param index   option index
     */
    public static void selectByIndex(WebElement element, int index) {
        toSelect(element).selectByIndex(index);
        LOGGER.debug("Dropdown option selected by index [{}]", index);
    }

    /**
     * Returns every option text present in the dropdown.
     *
     * @param element the select element
     * @return the option labels in document order
     */
    public static List<String> getAllOptions(WebElement element) {
        return toSelect(element).getOptions().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }

    /**
     * Returns the currently selected option text.
     *
     * @param element the select element
     * @return the selected label
     */
    public static String getSelectedOption(WebElement element) {
        return toSelect(element).getFirstSelectedOption().getText().trim();
    }

    /**
     * @param element the select element
     * @return {@code true} when the dropdown supports multiple selection
     */
    public static boolean isMultiSelect(WebElement element) {
        return toSelect(element).isMultiple();
    }

    /**
     * Clears every selection in a multi select dropdown.
     *
     * @param element the select element
     */
    public static void deselectAll(WebElement element) {
        toSelect(element).deselectAll();
        LOGGER.debug("All dropdown selections cleared");
    }
}
