package com.enterprise.automation.utilities;

import com.enterprise.automation.driver.DriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Optional;

public final class AlertUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(AlertUtility.class);

    private AlertUtility() {
        throw new IllegalStateException("AlertUtility is a utility class and must not be instantiated");
    }

    public static Optional<Alert> waitForAlert() {
        try {
            return Optional.of(WaitUtility.explicitWait().until(ExpectedConditions.alertIsPresent()));
        } catch (TimeoutException | NoAlertPresentException exception) {
            LOGGER.debug("No alert appeared within the explicit wait window");
            return Optional.empty();
        }
    }

    public static boolean isAlertPresent() {
        try {
            DriverManager.getDriver().switchTo().alert();
            return true;
        } catch (NoAlertPresentException noAlertPresentException) {
            return false;
        }
    }

    public static boolean accept() {
        return waitForAlert().map(alert -> {
            String text = alert.getText();
            alert.accept();
            LOGGER.info("Alert accepted | message = [{}]", text);
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }

    public static boolean dismiss() {
        return waitForAlert().map(alert -> {
            String text = alert.getText();
            alert.dismiss();
            LOGGER.info("Alert dismissed | message = [{}]", text);
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }

    public static Optional<String> getText() {
        return waitForAlert().map(Alert::getText);
    }

    public static boolean typeAndAccept(String text) {
        return waitForAlert().map(alert -> {
            alert.sendKeys(text);
            alert.accept();
            LOGGER.info("Prompt answered and accepted");
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }
}
