package com.enterprise.automation.zephyr;

import com.enterprise.automation.utilities.LoggerUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ZephyrClient {
    public static final String ESSENTIAL_BASE_URL = "https://prod-api.zephyr4jiracloud.com/v2";
    public static final String US_BASE_URL = "https://api.zephyrscale.smartbear.com/v2";
    public static final String EU_BASE_URL = "https://eu.api.zephyrscale.smartbear.com/v2";
    private static final Logger LOGGER = LoggerUtility.getLogger(ZephyrClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_ATTEMPTS = 3;
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 200;
    private static final long BASE_BACKOFF_MILLIS = 1_000L;
    private static final long MAX_BACKOFF_MILLIS = 30_000L;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_SERVER_ERROR = 500;
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String KEY = "key";
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String apiToken;
    private final Duration timeout;

    public ZephyrClient(String baseUrl, String apiToken, Duration timeout) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.timeout = timeout;
        HttpClient.Builder builder = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(timeout).followRedirects(HttpClient.Redirect.NORMAL);
        Optional.ofNullable(ProxySelector.getDefault()).ifPresent(builder::proxy);
        this.httpClient = builder.build();
    }

    public String baseUrl() {
        return baseUrl;
    }

    public int probeProject(String projectKey) {
        JsonNode response = execute(GET, "/testcases?projectKey=" + encode(projectKey) + "&maxResults=1", null);
        return response.path("total").asInt(-1);
    }

    public String createTestCycle(Map<String, Object> body) {
        return requireKey(execute(POST, "/testcycles", body), "/testcycles");
    }

    public String createTestCase(Map<String, Object> body) {
        return requireKey(execute(POST, "/testcases", body), "/testcases");
    }

    public boolean hasExecution(String projectKey, String testCycleKey, String testCaseKey) {
        JsonNode response = execute(GET, "/testexecutions?projectKey=" + encode(projectKey)
                + "&testCycle=" + encode(testCycleKey) + "&testCase=" + encode(testCaseKey)
                + "&maxResults=1", null, 1);
        JsonNode values = response.path("values");
        return (values.isArray() && !values.isEmpty()) || response.path("total").asInt(0) > 0;
    }

    public JsonNode createTestExecution(Map<String, Object> body) {
        return execute(POST, "/testexecutions", body);
    }

    public Map<String, String> listTestCaseKeysByName(String projectKey) {
        Map<String, String> keysByName = new LinkedHashMap<>();
        int startAt = 0;
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode response = execute(GET, "/testcases?projectKey=" + encode(projectKey)
                    + "&maxResults=" + PAGE_SIZE + "&startAt=" + startAt, null);
            JsonNode values = response.path("values");
            if (!values.isArray() || values.isEmpty()) {
                break;
            }
            for (JsonNode testCase : values) {
                String key = testCase.path(KEY).asText("");
                String name = testCase.path("name").asText("");
                if (!key.isEmpty() && !name.isEmpty()) {
                    keysByName.putIfAbsent(normaliseName(name), key);
                }
            }
            startAt += values.size();
            boolean isLast = response.path("isLast").asBoolean(false);
            int total = response.path("total").asInt(Integer.MAX_VALUE);
            if (isLast || startAt >= total) {
                break;
            }
        }
        LOGGER.info("Zephyr: {} existing test case(s) found in project [{}]", keysByName.size(), projectKey);
        return keysByName;
    }

    public static String normaliseName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private JsonNode execute(String method, String path, Object body) {
        return execute(method, path, body, MAX_ATTEMPTS);
    }

    private JsonNode execute(String method, String path, Object body, int maxAttempts) {
        String url = baseUrl + path;
        String json = body == null ? null : toJson(body);
        boolean readOnly = GET.equals(method);
        for (int attempt = 1; ; attempt++) {
            HttpRequest request = buildRequest(method, url, json);
            long started = System.nanoTime();
            try {
                HttpResponse<String> response = sendWithHardTimeout(request);
                int status = response.statusCode();
                LOGGER.info("Zephyr {} {} -> HTTP {} ({} ms)", method, path, status, elapsedMillis(started));
                if (status >= 200 && status < 300) {
                    return parse(method, url, response.body());
                }
                boolean retriable = status == HTTP_TOO_MANY_REQUESTS || status >= HTTP_SERVER_ERROR;
                if (retriable && attempt < maxAttempts) {
                    long waitMillis = backoffMillis(attempt, response.headers().firstValue("Retry-After"));
                    LOGGER.warn("Zephyr {} {} answered HTTP {}; retrying in {} ms", method, path, status, waitMillis);
                    sleep(waitMillis);
                    continue;
                }
                throw new ZephyrApiException(method, url, status, response.body(), null);
            } catch (IOException ioException) {
                LOGGER.warn("Zephyr {} {} failed after {} ms: {}", method, path, elapsedMillis(started),
                        ioException.toString());
                if (readOnly && attempt < maxAttempts) {
                    long waitMillis = backoffMillis(attempt, Optional.empty());
                    LOGGER.warn("Zephyr {} {}: retrying in {} ms", method, path, waitMillis);
                    sleep(waitMillis);
                    continue;
                }
                throw new ZephyrApiException(method, url, ZephyrApiException.NO_RESPONSE, "", ioException);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new ZephyrApiException(method, url, ZephyrApiException.NO_RESPONSE, "", interruptedException);
            }
        }
    }

    private HttpResponse<String> sendWithHardTimeout(HttpRequest request) throws IOException, InterruptedException {
        CompletableFuture<HttpResponse<String>> future =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException timedOut) {
            future.cancel(true);
            throw new HttpTimeoutException("no complete response within " + timeout.toSeconds() + " s");
        } catch (ExecutionException failed) {
            Throwable cause = failed.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException(cause == null ? failed : cause);
        }
    }

    private static long elapsedMillis(long startedNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
    }

    private HttpRequest buildRequest(String method, String url, String json) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(timeout)
                .header("Authorization", "Bearer " + apiToken).header("Accept", "application/json");
        if (json == null) {
            return builder.method(method, HttpRequest.BodyPublishers.noBody()).build();
        }
        return builder.header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();
    }

    private static JsonNode parse(String method, String url, String body) {
        if (body == null || body.isBlank()) {
            return MAPPER.createObjectNode();
        }
        try {
            return MAPPER.readTree(body);
        } catch (JsonProcessingException jsonProcessingException) {
            throw new ZephyrApiException(method, url, ZephyrApiException.NO_RESPONSE,
                    "Response was not JSON: " + body, jsonProcessingException);
        }
    }

    private static String toJson(Object body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException jsonProcessingException) {
            throw new IllegalArgumentException("Unable to serialise Zephyr request body", jsonProcessingException);
        }
    }

    private static String requireKey(JsonNode response, String path) {
        String key = response.path(KEY).asText("");
        if (key.isEmpty()) {
            throw new ZephyrApiException(POST, path, ZephyrApiException.NO_RESPONSE,
                    "Response did not contain a key: " + response, null);
        }
        return key;
    }

    private static long backoffMillis(int attempt, Optional<String> retryAfterHeader) {
        long fromHeader = retryAfterHeader.map(String::trim).filter(value -> value.matches("\\d+"))
                .map(seconds -> Long.parseLong(seconds) * 1_000L).orElse(0L);
        long exponential = BASE_BACKOFF_MILLIS << (attempt - 1);
        return Math.min(Math.max(fromHeader, exponential), MAX_BACKOFF_MILLIS);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
