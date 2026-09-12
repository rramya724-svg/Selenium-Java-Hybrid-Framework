package com.enterprise.automation.email;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.reports.ExecutionSummary;
import com.enterprise.automation.reports.ReportManager;
import com.enterprise.automation.utilities.DateUtility;
import com.enterprise.automation.utilities.LoggerUtility;
import com.enterprise.automation.utilities.ScreenshotUtility;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Sends the post execution notification email with every generated artefact attached.
 *
 * <p>The utility is fail safe by design. A broken SMTP configuration must never
 * turn a green suite red, so all messaging failures are logged and swallowed
 * rather than propagated. Sending is disabled by default and switched on with
 * {@code email.enabled=true}.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class EmailUtility {

    private static final Logger LOGGER = LoggerUtility.getLogger(EmailUtility.class);

    private static final String PROPERTY_SMTP_HOST = "mail.smtp.host";
    private static final String PROPERTY_SMTP_PORT = "mail.smtp.port";
    private static final String PROPERTY_SMTP_AUTH = "mail.smtp.auth";
    private static final String PROPERTY_SMTP_STARTTLS = "mail.smtp.starttls.enable";
    private static final String PROPERTY_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    private static final String PROPERTY_SMTP_SSL_PROTOCOLS = "mail.smtp.ssl.protocols";
    private static final String PROPERTY_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";
    private static final String TLS_PROTOCOLS = "TLSv1.2 TLSv1.3";
    private static final String ADDRESS_DELIMITER = ",";
    private static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final int MAX_SCREENSHOT_ATTACHMENTS = 10;

    private EmailUtility() {
        throw new IllegalStateException("EmailUtility is a utility class and must not be instantiated");
    }

    /**
     * Builds and sends the execution summary email when {@code email.enabled} is true.
     */
    public static void sendExecutionReport() {
        if (!ConfigReader.getBoolean(ConfigKey.EMAIL_ENABLED, false)) {
            LOGGER.info("Email notification is disabled; set email.enabled=true in config.properties to enable it");
            return;
        }
        try {
            Session session = buildSession();
            MimeMessage message = buildMessage(session);
            Transport.send(message);
            LOGGER.info("Execution report email sent successfully to [{}]",
                    ConfigReader.get(ConfigKey.EMAIL_TO, FrameworkConstants.EMPTY_STRING));
        } catch (MessagingException messagingException) {
            LOGGER.error("Unable to send the execution report email", messagingException);
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Unexpected failure while preparing the execution report email", runtimeException);
        }
    }

    private static Session buildSession() {
        Properties properties = new Properties();
        properties.put(PROPERTY_SMTP_HOST, ConfigReader.get(ConfigKey.EMAIL_SMTP_HOST));
        properties.put(PROPERTY_SMTP_PORT, ConfigReader.get(ConfigKey.EMAIL_SMTP_PORT));
        properties.put(PROPERTY_SMTP_AUTH, String.valueOf(ConfigReader.getBoolean(ConfigKey.EMAIL_SMTP_AUTH, true)));
        properties.put(PROPERTY_SMTP_STARTTLS,
                String.valueOf(ConfigReader.getBoolean(ConfigKey.EMAIL_SMTP_STARTTLS, true)));
        properties.put(PROPERTY_SMTP_SSL_ENABLE,
                String.valueOf(ConfigReader.getBoolean(ConfigKey.EMAIL_SMTP_SSL, false)));
        properties.put(PROPERTY_SMTP_SSL_PROTOCOLS, TLS_PROTOCOLS);
        properties.put(PROPERTY_SMTP_SSL_TRUST, ConfigReader.get(ConfigKey.EMAIL_SMTP_HOST));

        final String username = ConfigReader.get(ConfigKey.EMAIL_USERNAME);
        final String password = ConfigReader.get(ConfigKey.EMAIL_PASSWORD);

        return Session.getInstance(properties, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private static MimeMessage buildMessage(Session session) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(ConfigReader.get(ConfigKey.EMAIL_FROM)));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(ConfigReader.get(ConfigKey.EMAIL_TO)));

        String carbonCopy = ConfigReader.get(ConfigKey.EMAIL_CC, FrameworkConstants.EMPTY_STRING);
        if (StringUtils.isNotBlank(carbonCopy)) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(carbonCopy));
        }

        message.setSubject(buildSubject());
        message.setSentDate(new java.util.Date());

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(buildHtmlBody(), HTML_CONTENT_TYPE);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        attachArtefacts(multipart);

        message.setContent(multipart);
        return message;
    }

    private static String buildSubject() {
        String status = ExecutionSummary.getFailed() == 0 ? FrameworkConstants.STATUS_PASS
                : FrameworkConstants.STATUS_FAIL;
        return ConfigReader.get(ConfigKey.EMAIL_SUBJECT, "Automation Execution Report")
                + " | " + ConfigReader.get(ConfigKey.ENVIRONMENT, "QA")
                + " | " + status
                + " | " + DateUtility.currentDisplayTimestamp();
    }

    private static String buildHtmlBody() {
        String accentColour = ExecutionSummary.getFailed() == 0 ? "#2e7d32" : "#c62828";
        return """
                <html><body style="font-family:Segoe UI,Arial,sans-serif;font-size:14px;color:#212121;">
                  <h2 style="color:%s;margin-bottom:4px;">Automation Execution Summary</h2>
                  <p style="margin-top:0;color:#616161;">%s &nbsp;|&nbsp; Framework Version %s</p>
                  <table cellpadding="8" cellspacing="0" style="border-collapse:collapse;border:1px solid #e0e0e0;">
                    <tr style="background:#f5f5f5;"><td><b>Environment</b></td><td>%s</td></tr>
                    <tr><td><b>Browser</b></td><td>%s</td></tr>
                    <tr style="background:#f5f5f5;"><td><b>Operating System</b></td><td>%s</td></tr>
                    <tr><td><b>Java Version</b></td><td>%s</td></tr>
                    <tr style="background:#f5f5f5;"><td><b>Executed On</b></td><td>%s</td></tr>
                    <tr><td><b>Total Duration</b></td><td>%s</td></tr>
                  </table>
                  <h3 style="margin-bottom:4px;">Results</h3>
                  <table cellpadding="8" cellspacing="0" style="border-collapse:collapse;border:1px solid #e0e0e0;">
                    <tr style="background:#eeeeee;"><th>Total</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>Retries</th><th>Pass %%</th></tr>
                    <tr align="center"><td>%d</td><td style="color:#2e7d32;"><b>%d</b></td><td style="color:#c62828;"><b>%d</b></td><td style="color:#ef6c00;"><b>%d</b></td><td>%d</td><td><b>%s%%</b></td></tr>
                  </table>
                  <p style="margin-top:18px;color:#616161;">The Extent report, TestNG reports, Surefire results, execution log and screenshots are attached.</p>
                  <p style="color:#9e9e9e;font-size:12px;">Generated automatically by EnterpriseAutomationFramework &middot; Prepared by Lalith Kumar BV</p>
                </body></html>
                """.formatted(
                accentColour,
                ConfigReader.get(ConfigKey.APPLICATION_NAME, "EnterpriseAutomationFramework"),
                ConfigReader.get(ConfigKey.RELEASE_VERSION, "1.0"),
                ConfigReader.get(ConfigKey.ENVIRONMENT, "QA"),
                ConfigReader.get(ConfigKey.BROWSER, "chrome").toUpperCase(java.util.Locale.ROOT),
                System.getProperty("os.name"),
                System.getProperty("java.version"),
                DateUtility.toDisplayTimestamp(ExecutionSummary.getStartMillis()),
                DateUtility.formatDuration(ExecutionSummary.getDurationMillis()),
                ExecutionSummary.getTotal(),
                ExecutionSummary.getPassed(),
                ExecutionSummary.getFailed(),
                ExecutionSummary.getSkipped(),
                ExecutionSummary.getRetried(),
                String.valueOf(ExecutionSummary.getPassPercentage()));
    }

    private static void attachArtefacts(Multipart multipart) {
        for (Path candidate : collectReportArtefacts()) {
            attachFile(multipart, candidate);
        }
        if (ConfigReader.getBoolean(ConfigKey.EMAIL_ATTACH_SCREENSHOTS, true)) {
            collectScreenshots().forEach(screenshot -> attachFile(multipart, screenshot));
        }
    }

    private static List<Path> collectReportArtefacts() {
        Path outputDirectory = Path.of(FrameworkConstants.OUTPUT_DIRECTORY);
        List<Path> artefacts = new ArrayList<>();
        Stream.of(
                ReportManager.getReportFilePath(),
                outputDirectory.resolve(FrameworkConstants.EMAILABLE_REPORT_FILE_NAME),
                outputDirectory.resolve(FrameworkConstants.TESTNG_INDEX_REPORT_FILE_NAME),
                Path.of(FrameworkConstants.SUREFIRE_REPORT_DIRECTORY)
                        .resolve(FrameworkConstants.EMAILABLE_REPORT_FILE_NAME),
                Path.of(ConfigReader.get(ConfigKey.LOG_PATH, "logs"))
                        .resolve(FrameworkConstants.EXECUTION_LOG_FILE_NAME)
        ).filter(java.util.Objects::nonNull)
         .filter(Files::isRegularFile)
         .forEach(artefacts::add);
        return artefacts;
    }

    private static List<Path> collectScreenshots() {
        Path screenshotDirectory = ScreenshotUtility.screenshotDirectory();
        if (!Files.isDirectory(screenshotDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(screenshotDirectory)) {
            return files.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(FrameworkConstants.SCREENSHOT_EXTENSION))
                        .sorted()
                        .limit(MAX_SCREENSHOT_ATTACHMENTS)
                        .toList();
        } catch (IOException ioException) {
            LOGGER.warn("Unable to list the screenshot directory for email attachments", ioException);
            return List.of();
        }
    }

    private static void attachFile(Multipart multipart, Path file) {
        try {
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(file.toFile());
            attachment.setFileName(file.getFileName().toString());
            multipart.addBodyPart(attachment);
            LOGGER.debug("Attached [{}] to the execution report email", file.getFileName());
        } catch (IOException | MessagingException exception) {
            LOGGER.warn("Unable to attach [{}] to the execution report email", file, exception);
        }
    }
}
