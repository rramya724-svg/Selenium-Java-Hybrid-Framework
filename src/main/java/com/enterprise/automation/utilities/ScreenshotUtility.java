package com.enterprise.automation.utilities;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.driver.DriverManager;
import com.enterprise.automation.enums.ConfigKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Captures browser screenshots as files and as Base64 payloads.
 *
 * <p>File names embed the test name, the executing thread and a millisecond
 * timestamp, which guarantees uniqueness under {@code parallel="methods"}.
 * Every method degrades gracefully: a screenshot failure is logged but never
 * masks the real test failure.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class ScreenshotUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(ScreenshotUtility.class);
    private static final String ILLEGAL_FILE_NAME_CHARACTERS = "[^a-zA-Z0-9._-]";
    private static final String REPLACEMENT_CHARACTER = "_";

    private ScreenshotUtility() {
        throw new IllegalStateException("ScreenshotUtility is a utility class and must not be instantiated");
    }

    /**
     * Captures the current viewport and writes it to the configured screenshot directory.
     *
     * @param testName logical name used to build the file name
     * @return the absolute path of the written file, empty when capture failed
     */
    public static Optional<String> captureToFile(String testName) {
        if (!DriverManager.isDriverInitialised()) {
            LOGGER.warn("Screenshot skipped for [{}] : no driver bound to this thread", testName);
            return Optional.empty();
        }
        try {
            WebDriver driver = DriverManager.getDriver();
            byte[] imageBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            Path targetDirectory = screenshotDirectory();
            Files.createDirectories(targetDirectory);

            Path targetFile = targetDirectory.resolve(buildFileName(testName));
            Files.write(targetFile, imageBytes);

            String absolutePath = targetFile.toAbsolutePath().toString();
            LOGGER.info("Screenshot captured for [{}] -> {}", testName, absolutePath);
            return Optional.of(absolutePath);
        } catch (IOException ioException) {
            LOGGER.error("Unable to write the screenshot file for [{}]", testName, ioException);
            return Optional.empty();
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Unable to capture a screenshot for [{}]", testName, runtimeException);
            return Optional.empty();
        }
    }

    /**
     * Captures the current viewport as a Base64 string suitable for embedding
     * directly inside the Extent report.
     *
     * @return the Base64 payload, empty when capture failed
     */
    public static Optional<String> captureAsBase64() {
        if (!DriverManager.isDriverInitialised()) {
            return Optional.empty();
        }
        try {
            WebDriver driver = DriverManager.getDriver();
            return Optional.of(((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64));
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Unable to capture a Base64 screenshot", runtimeException);
            return Optional.empty();
        }
    }

    /**
     * @return the absolute screenshot directory resolved from configuration
     */
    public static Path screenshotDirectory() {
        String configured = ConfigReader.get(ConfigKey.SCREENSHOT_PATH, "test-output/Screenshots");
        Path path = Path.of(configured);
        return path.isAbsolute() ? path : Path.of(FrameworkConstants.USER_DIRECTORY).resolve(configured);
    }

    private static String buildFileName(String testName) {
        String sanitised = StringUtils.defaultIfBlank(testName, "screenshot")
                .replaceAll(ILLEGAL_FILE_NAME_CHARACTERS, REPLACEMENT_CHARACTER);
        String threadName = Thread.currentThread().getName()
                .replaceAll(ILLEGAL_FILE_NAME_CHARACTERS, REPLACEMENT_CHARACTER);
        return sanitised + REPLACEMENT_CHARACTER + threadName + REPLACEMENT_CHARACTER
                + DateUtility.currentFileTimestamp() + FrameworkConstants.SCREENSHOT_EXTENSION;
    }
}
