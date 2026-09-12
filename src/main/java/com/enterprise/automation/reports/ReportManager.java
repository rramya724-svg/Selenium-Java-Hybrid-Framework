package com.enterprise.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.exceptions.FrameworkException;
import com.enterprise.automation.utilities.DateUtility;
import com.enterprise.automation.utilities.LoggerUtility;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Owns the lifecycle of the single {@link ExtentReports} instance.
 *
 * <p>{@code ExtentReports} is thread safe for concurrent {@code createTest}
 * calls, so exactly one instance is shared across every parallel thread and
 * flushed once when the suite finishes. Creation is guarded by double checked
 * locking on a {@code volatile} field.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class ReportManager {

    private static final Logger LOGGER = LoggerUtility.getLogger(ReportManager.class);
    private static final Object LOCK = new Object();

    private static volatile ExtentReports extentReports;
    private static volatile Path reportFilePath;

    private ReportManager() {
        throw new IllegalStateException("ReportManager is a utility class and must not be instantiated");
    }

    /**
     * Returns the shared reporter, creating and configuring it on first use.
     *
     * @return the singleton {@link ExtentReports} instance
     */
    public static ExtentReports getInstance() {
        ExtentReports local = extentReports;
        if (local == null) {
            synchronized (LOCK) {
                local = extentReports;
                if (local == null) {
                    local = createReporter();
                    extentReports = local;
                }
            }
        }
        return local;
    }

    private static ExtentReports createReporter() {
        Path directory = reportDirectory();
        try {
            Files.createDirectories(directory);
        } catch (IOException ioException) {
            throw new FrameworkException("Unable to create the report directory: " + directory, ioException);
        }

        boolean overrideReports = ConfigReader.getBoolean(ConfigKey.OVERRIDE_REPORTS, true);
        String fileName = overrideReports
                ? FrameworkConstants.EXTENT_REPORT_FILE_NAME
                : "ExtentReport_" + DateUtility.currentFileTimestamp() + ".html";

        reportFilePath = directory.resolve(fileName);

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFilePath.toFile());
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Enterprise Automation Execution Report");
        sparkReporter.config().setReportName(ConfigReader.get(ConfigKey.APPLICATION_NAME, "EnterpriseAutomationFramework")
                + " | " + ConfigReader.get(ConfigKey.ENVIRONMENT, "QA"));
        sparkReporter.config().setTimeStampFormat(FrameworkConstants.DISPLAY_TIMESTAMP_PATTERN);
        sparkReporter.config().setEncoding("UTF-8");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(sparkReporter);
        attachSystemInformation(reports);

        LOGGER.info("Extent report initialised at {}", reportFilePath);
        return reports;
    }

    private static void attachSystemInformation(ExtentReports reports) {
        reports.setSystemInfo("Project", ConfigReader.get(ConfigKey.APPLICATION_NAME, "EnterpriseAutomationFramework"));
        reports.setSystemInfo("Framework Version", ConfigReader.get(ConfigKey.RELEASE_VERSION, "1.0"));
        reports.setSystemInfo("Prepared By", "Lalith Kumar BV");
        reports.setSystemInfo("Environment", ConfigReader.get(ConfigKey.ENVIRONMENT, "QA"));
        reports.setSystemInfo("Browser", ConfigReader.get(ConfigKey.BROWSER, "chrome").toUpperCase(java.util.Locale.ROOT));
        reports.setSystemInfo("Headless", String.valueOf(ConfigReader.getBoolean(ConfigKey.HEADLESS, false)));
        reports.setSystemInfo("Operating System",
                System.getProperty("os.name") + " " + System.getProperty("os.version"));
        reports.setSystemInfo("System Architecture", System.getProperty("os.arch"));
        reports.setSystemInfo("Java Version", System.getProperty("java.version"));
        reports.setSystemInfo("Java Vendor", System.getProperty("java.vendor"));
        reports.setSystemInfo("Executed By", System.getProperty("user.name"));
        reports.setSystemInfo("Execution Started", DateUtility.currentDisplayTimestamp());
        reports.setSystemInfo("Available Processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    /**
     * Writes every buffered log entry to disk. Safe to call more than once.
     */
    public static void flush() {
        ExtentReports local = extentReports;
        if (local != null) {
            local.flush();
            LOGGER.info("Extent report flushed to {}", reportFilePath);
        }
    }

    /**
     * @return the absolute path of the generated Extent report, {@code null} before initialisation
     */
    public static Path getReportFilePath() {
        return reportFilePath;
    }

    /**
     * @return the absolute report directory resolved from configuration
     */
    public static Path reportDirectory() {
        String configured = ConfigReader.get(ConfigKey.REPORT_PATH, "test-output/ExtentReports");
        Path path = Path.of(configured);
        return path.isAbsolute() ? path : Path.of(FrameworkConstants.USER_DIRECTORY).resolve(configured);
    }
}
