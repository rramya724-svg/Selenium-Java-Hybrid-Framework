package com.enterprise.automation.utilities;

import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class WindowUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(WindowUtility.class);

    private WindowUtility() {
        throw new IllegalStateException("WindowUtility is a utility class and must not be instantiated");
    }

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    public static String getCurrentWindowHandle() {
        return driver().getWindowHandle();
    }

    public static List<String> getAllWindowHandles() {
        return new ArrayList<>(driver().getWindowHandles());
    }

    public static String switchToNewestWindow() {
        List<String> handles = getAllWindowHandles();
        if (handles.isEmpty()) {
            throw new FrameworkException("No browser window handles are available to switch to");
        }
        String newest = handles.get(handles.size() - 1);
        driver().switchTo().window(newest);
        LOGGER.info("Switched to the newest window handle [{}]", newest);
        return newest;
    }

    public static String switchToWindowByTitle(String titleFragment) {
        String origin = getCurrentWindowHandle();
        Set<String> handles = driver().getWindowHandles();
        for (String handle : handles) {
            driver().switchTo().window(handle);
            if (driver().getTitle() != null && driver().getTitle().contains(titleFragment)) {
                LOGGER.info("Switched to window with title containing [{}]", titleFragment);
                return handle;
            }
        }
        driver().switchTo().window(origin);
        throw new FrameworkException("No open window has a title containing '" + titleFragment + "'");
    }

    public static void closeAllWindowsExcept(String handleToKeep) {
        for (String handle : driver().getWindowHandles()) {
            if (!handle.equals(handleToKeep)) {
                driver().switchTo().window(handle);
                driver().close();
                LOGGER.debug("Closed window handle [{}]", handle);
            }
        }
        driver().switchTo().window(handleToKeep);
        LOGGER.info("Focus restored to window handle [{}]", handleToKeep);
    }

    public static String openNewTab(String url) {
        driver().switchTo().newWindow(org.openqa.selenium.WindowType.TAB).get(url);
        String handle = getCurrentWindowHandle();
        LOGGER.info("Opened a new tab [{}] and navigated to {}", handle, url);
        return handle;
    }
}
