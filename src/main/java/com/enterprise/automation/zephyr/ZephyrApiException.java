package com.enterprise.automation.zephyr;

import java.io.Serial;

public class ZephyrApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final int NO_RESPONSE = -1;
    private static final int MAX_BODY_IN_MESSAGE = 500;
    private final int statusCode;
    private final String responseBody;

    public ZephyrApiException(String method, String url, int statusCode, String responseBody, Throwable cause) {
        super(method + " " + url + " -> "
                + (statusCode == NO_RESPONSE ? "no response" : "HTTP " + statusCode)
                + (responseBody == null || responseBody.isBlank() ? "" : " : " + abbreviate(responseBody)), cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody == null ? "" : responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }

    public boolean isAuthenticationProblem() {
        return statusCode == 401 || statusCode == 403;
    }

    private static String abbreviate(String text) {
        String singleLine = text.replaceAll("\\s+", " ").trim();
        return singleLine.length() <= MAX_BODY_IN_MESSAGE ? singleLine
                : singleLine.substring(0, MAX_BODY_IN_MESSAGE) + "...";
    }
}
