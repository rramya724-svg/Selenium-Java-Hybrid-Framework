package com.enterprise.automation.utilities;

import com.enterprise.automation.driver.DriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Optional;

/**
 * Helpers for native JavaScript alert, confirm and prompt dialogs.
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class AlertUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(AlertUtility.class);

    private AlertUtility() {
        throw new IllegalStateException("AlertUtility is a utility class and must not be instantiated");
    }

    /**
     * Waits for an alert and returns it when one appears.
     *
     * @return the alert, empty when none appeared inside the explicit wait window
     */
    public static Optional<Alert> waitForAlert() {
        try {
            return Optional.of(WaitUtility.explicitWait().until(ExpectedConditions.alertIsPresent()));
        } catch (TimeoutException | NoAlertPresentException exception) {
            LOGGER.debug("No alert appeared within the explicit wait window");
            return Optional.empty();
        }
    }

    /**
     * @return {@code true} when an alert is currently displayed
     */
    public static boolean isAlertPresent() {
        try {
            DriverManager.getDriver().switchTo().alert();
            return true;
        } catch (NoAlertPresentException noAlertPresentException) {
            return false;
        }
    }

    /**
     * Accepts the alert if one is present.
     *
     * @return {@code true} when an alert was accepted
     */
    public static boolean accept() {
        return waitForAlert().map(alert -> {
            String text = alert.getText();
            alert.accept();
            LOGGER.info("Alert accepted | message = [{}]", text);
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }

    /**
     * Dismisses the alert if one is present.
     *
     * @return {@code true} when an alert was dismissed
     */
    public static boolean dismiss() {
        return waitForAlert().map(alert -> {
            String text = alert.getText();
            alert.dismiss();
            LOGGER.info("Alert dismissed | message = [{}]", text);
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }

    /**
     * Reads the alert message without acting on the dialog.
     *
     * @return the alert text, empty when no alert is present
     */
    public static Optional<String> getText() {
        return waitForAlert().map(Alert::getText);
    }

    /**
     * Types into a prompt dialog and accepts it.
     *
     * @param text value to enter
     * @return {@code true} when the prompt was handled
     */
    public static boolean typeAndAccept(String text) {
        return waitForAlert().map(alert -> {
            alert.sendKeys(text);
            alert.accept();
            LOGGER.info("Prompt answered and accepted");
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }
}
