package com.enterprise.automation.utilities;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

public class UpdateZephyrTestExecution {
	 private UpdateZephyrTestExecution() {
	        throw new UnsupportedOperationException("Utility class");
	    }
	 private static final Logger logger = LogManager.getLogger(UpdateZephyrTestExecution.class);
    public static void updateTestExecution(ITestResult result, String status) {
        String apiUrl = "https://api.zephyrscale.smartbear.com/v2/testexecutions";
        String filePath = "token.txt"; 
        String bearerToken = readApiTokenFromFile(filePath);
        if (bearerToken == null) {
            logger.error("Failed to read the API token.");
            return;
        }
        if (status == null || status.isEmpty()) {
            logger.error("Warning: Status is null or empty. Skipping Zephyr update.");
            return;
        }
        status = status.equalsIgnoreCase("Pass") ? "Pass" : "Fail";
        String testCaseKey = getZephyrTestCaseId(result);
        String testCycleKey = getZephyrTestCycleId(result);
        if (testCaseKey == null || testCycleKey == null) {
            logger.error("Could not determine Zephyr Test Case ID or Test Cycle ID.");
            return;
        }
        logger.info("Updating Zephyr Test Case: {} in Cycle: {} with status: {}", testCaseKey, testCycleKey, status);
       makeAuthenticatedPostRequest(apiUrl, bearerToken, testCaseKey, testCycleKey, status);
    }
    private static String getZephyrTestCaseId(ITestResult result) {
        try {
            Method method = result.getMethod().getConstructorOrMethod().getMethod();
            ZephyrTestCase annotation = method.getAnnotation(ZephyrTestCase.class);
            return annotation != null ? annotation.id() : null;
        } catch (Exception e) {
        	logger.error("Error extracting Zephyr Test Case ID: {}", e.getMessage(), e);
            return null;
        }
    }
    private static String getZephyrTestCycleId(ITestResult result) {
        try {
            Method method = result.getMethod().getConstructorOrMethod().getMethod();
            ZephyrTestCycle annotation = method.getAnnotation(ZephyrTestCycle.class);
            return annotation != null ? annotation.id() : null;
        } catch (Exception e) {
        	logger.error("Error extracting Zephyr Test Cycle ID: {}", e.getMessage(), e);
            return null;
        }
    }
    private static String readApiTokenFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine().trim();
        } catch (IOException e) {
        	  logger.error("Error reading the API token file: {}", e.getMessage(), e);
            return null;
        }
    }
    @SuppressWarnings("unused")
	private static void makeAuthenticatedPostRequest(String apiUrl, String bearerToken, String testCaseKey, String testCycleKey, String status) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            String payload = """
                {
                    "projectKey": "IAPPAE",
                    "testCaseKey": "%s",
                    "testCycleKey": "%s",
                    "statusName": "%s"
                }
            """.formatted(testCaseKey, testCycleKey, status);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            logger.info("Zephyr API Response Code: {}", responseCode);

            try (Scanner scanner = new Scanner(
                responseCode >= 200 && responseCode < 300 
                    ? connection.getInputStream() 
                    : connection.getErrorStream(),
                StandardCharsets.UTF_8)) {
                String response = scanner.useDelimiter("\\A").next();
                logger.info("Zephyr API Response: {}", response);
            }
        } catch (IOException e) {
        	logger.error("Error during the request: {}", e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
