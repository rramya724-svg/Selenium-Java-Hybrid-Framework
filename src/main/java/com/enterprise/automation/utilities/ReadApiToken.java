package com.enterprise.automation.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class ReadApiToken {
	private static final Logger logger = LogManager.getLogger(ReadApiToken.class);
	public static void main(String[] args) {
		String filePath = "token.txt";
		@SuppressWarnings("unused")
		String apiToken = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            apiToken = reader.readLine().trim();
            logger.info("API Token: {}", apiToken);
        } catch (IOException e) {
            logger.error("Failed to read API token from file: {}", filePath, e);
        }
    }
}
