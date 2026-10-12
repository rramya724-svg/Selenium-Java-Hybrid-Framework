package com.enterprise.automation.zephyr;

import com.enterprise.automation.utilities.LoggerUtility;
import com.enterprise.automation.zephyr.ZephyrExecutionRecord.Outcome;
import org.apache.logging.log4j.Logger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ZephyrSyncService {
    private static final Logger LOGGER = LoggerUtility.getLogger(ZephyrSyncService.class);
    private static final String BANNER_LINE =
            "==============================================================================";
    private static final String LINE_BREAK = "<br>";
    private static final String TEST_CASE_LABEL = "automated";
    private static final String CYCLE_STATUS_DONE = "Done";
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_FAILURE_LENGTH = 2_000;
    private static final String SUMMARY_LINE = "  %-13s %-11s %s";
    private static final DateTimeFormatter CYCLE_TIMESTAMP =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
    private static final String PROJECT_KEY = "projectKey";
    private static final String TEST_CASE_KEY = "testCaseKey";
    private static final String TEST_CYCLE_KEY = "testCycleKey";
    private static final String STATUS_NAME = "statusName";
    private static final String COMMENT = "comment";
    private static final String ENVIRONMENT_NAME = "environmentName";
    private static final String EXECUTED_BY_ID = "executedById";
    private final ZephyrSettings settings;
    private final List<String> candidateOverride;
    private boolean optionalExecutionFieldsRejected;

    public ZephyrSyncService(ZephyrSettings settings) {
        this(settings, List.of());
    }

    ZephyrSyncService(ZephyrSettings settings, List<String> candidateOverride) {
        this.settings = settings;
        this.candidateOverride = List.copyOf(candidateOverride);
    }

    public record SyncResult(String testCycleKey, int posted, int manualAdded, int failed, int unmapped,
                             String skippedReason) {
        static SyncResult skipped(String reason) {
            return new SyncResult("", 0, 0, 0, 0, reason);
        }

        public boolean wasSkipped() {
            return !skippedReason.isEmpty();
        }
    }

    public SyncResult sync(ZephyrRunInfo run, List<ZephyrExecutionRecord> records) {
        if (records.isEmpty()) {
            LOGGER.info("Zephyr: no test results to report");
            return SyncResult.skipped("no test results");
        }
        if (!settings.hasToken()) {
            LOGGER.warn("Zephyr: sync skipped because no API token was found for Windows/OS user [{}]: {} is not "
                    + "set for this run and was not saved with setx for this user. Fix: run "
                    + "setx {} \"<token>\" as this user, or add it under Run Configurations > Environment in "
                    + "Eclipse, or pass -Dzephyr.api.token=<token> (see ZEPHYR_SETUP.md).",
                    System.getProperty("user.name"), ZephyrSettings.TOKEN_ENVIRONMENT_VARIABLE,
                    ZephyrSettings.TOKEN_ENVIRONMENT_VARIABLE);
            return SyncResult.skipped("no API token");
        }
        if (settings.projectKey().isBlank()) {
            LOGGER.warn("Zephyr: sync skipped because zephyr.project.key is blank in config.properties");
            return SyncResult.skipped("no project key");
        }
        Optional<ZephyrTokenInfo> tokenInfo = ZephyrTokenInfo.parse(settings.apiToken());
        if (tokenInfo.isPresent() && tokenInfo.get().isExpired()) {
            LOGGER.error("Zephyr: sync skipped because the API token expired on {}. "
                    + "Create a new one on the Zephyr API Access Tokens page in Jira.", tokenInfo.get().expiresAt());
            return SyncResult.skipped("API token expired");
        }
        tokenInfo.map(ZephyrTokenInfo::jiraBaseUrl).filter(url -> !url.isEmpty())
                 .ifPresent(url -> LOGGER.info("Zephyr: token belongs to Jira site {}", url));
        Optional<ZephyrClient> connected = connect(candidateBaseUrls(tokenInfo));
        if (connected.isEmpty()) {
            return SyncResult.skipped("could not connect to Zephyr");
        }
        ZephyrClient client = connected.get();
        String testCycleKey;
        try {
            testCycleKey = resolveTestCycle(client, run, records);
        } catch (ZephyrApiException cycleProblem) {
            LOGGER.error("Zephyr: could not create the test cycle, nothing was reported. {}", cycleProblem.getMessage());
            return SyncResult.skipped("test cycle could not be created");
        }
        String accountId = tokenInfo.map(ZephyrTokenInfo::accountId).orElse("");
        return postExecutions(client, testCycleKey, run, records, accountId);
    }

    private List<String> candidateBaseUrls(Optional<ZephyrTokenInfo> tokenInfo) {
        if (!settings.baseUrl().isBlank()) {
            return List.of(settings.baseUrl());
        }
        if (!candidateOverride.isEmpty()) {
            return candidateOverride;
        }
        boolean essentialToken = tokenInfo.map(ZephyrTokenInfo::isZephyrEssential).orElse(false);
        return essentialToken
                ? List.of(ZephyrClient.ESSENTIAL_BASE_URL, ZephyrClient.US_BASE_URL, ZephyrClient.EU_BASE_URL)
                : List.of(ZephyrClient.US_BASE_URL, ZephyrClient.EU_BASE_URL, ZephyrClient.ESSENTIAL_BASE_URL);
    }

    private Optional<ZephyrClient> connect(List<String> candidates) {
        List<String> problems = new ArrayList<>();
        boolean anyAuthenticationProblem = false;
        boolean anyProjectProblem = false;
        boolean anyNoResponse = false;
        for (String candidate : candidates) {
            ZephyrClient client = new ZephyrClient(candidate, settings.apiToken(), settings.timeout());
            try {
                int total = client.probeProject(settings.projectKey());
                LOGGER.info("Zephyr: connected to {} | project [{}]{}", candidate, settings.projectKey(),
                        total >= 0 ? " | " + total + " test case(s)" : "");
                return Optional.of(client);
            } catch (ZephyrApiException problem) {
                problems.add("  " + problem.getMessage());
                anyAuthenticationProblem |= problem.isAuthenticationProblem();
                anyProjectProblem |= problem.statusCode() == HTTP_NOT_FOUND || problem.statusCode() == HTTP_BAD_REQUEST;
                anyNoResponse |= problem.statusCode() == ZephyrApiException.NO_RESPONSE;
            }
        }
        LOGGER.error("Zephyr: could not open project [{}]; nothing was reported.\n{}",
                settings.projectKey(), String.join("\n", problems));
        if (anyProjectProblem) {
            LOGGER.error("Zephyr hint: check that zephyr.project.key is right and that Zephyr is enabled for that "
                    + "project in Jira.");
        }
        if (anyAuthenticationProblem) {
            LOGGER.error("Zephyr hint: the API token was rejected. Create a new one on the Zephyr API Access "
                    + "Tokens page in Jira and update {}.", ZephyrSettings.TOKEN_ENVIRONMENT_VARIABLE);
        }
        if (anyNoResponse) {
            LOGGER.error("Zephyr hint: the API could not be reached. Check internet / proxy access to {} "
                    + "(proxy users: -Dhttps.proxyHost=... -Dhttps.proxyPort=...).", candidates);
        }
        return Optional.empty();
    }

    private String resolveTestCycle(ZephyrClient client, ZephyrRunInfo run, List<ZephyrExecutionRecord> records) {
        if (settings.usesFixedTestCycle()) {
            LOGGER.info("Zephyr: reporting into existing test cycle [{}]", settings.testCycleKey());
            return settings.testCycleKey();
        }
        String name = truncate(settings.testCycleNamePrefix() + " | " + run.suiteName() + " | "
                + run.environment() + " | " + displayTime(run.startMillis()), MAX_NAME_LENGTH);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(PROJECT_KEY, settings.projectKey());
        body.put("name", name);
        body.put("description", cycleDescription(run, records, settings.manualTestCaseKeys().size()));
        body.put("plannedStartDate", isoSeconds(run.startMillis()));
        body.put("plannedEndDate", isoSeconds(Math.max(run.endMillis(), run.startMillis())));
        body.put(STATUS_NAME, CYCLE_STATUS_DONE);
        String key;
        try {
            key = client.createTestCycle(body);
        } catch (ZephyrApiException problem) {
            if (problem.statusCode() != HTTP_BAD_REQUEST) {
                throw problem;
            }
            LOGGER.debug("Zephyr: cycle status '{}' rejected, retrying without it", CYCLE_STATUS_DONE);
            body.remove(STATUS_NAME);
            key = client.createTestCycle(body);
        }
        LOGGER.info("Zephyr: created test cycle [{}] '{}'", key, name);
        return key;
    }

    private SyncResult postExecutions(ZephyrClient client,
                                      String testCycleKey,
                                      ZephyrRunInfo run,
                                      List<ZephyrExecutionRecord> records,
                                      String accountId) {
        Map<String, String> keysByName = new LinkedHashMap<>();
        boolean lookupFailed = false;
        if (records.stream().anyMatch(r -> !r.hasExplicitTestCaseKey())) {
            try {
                keysByName.putAll(client.listTestCaseKeysByName(settings.projectKey()));
            } catch (ZephyrApiException problem) {
                lookupFailed = true;
                LOGGER.error("Zephyr: could not list test cases, tests without @ZephyrTestCase are not reported. {}",
                        problem.getMessage());
            }
        }
        int posted = 0;
        int failed = 0;
        int unmapped = 0;
        List<String> lines = new ArrayList<>();
        Set<String> reportedKeys = new LinkedHashSet<>();
        List<ZephyrExecutionRecord> ordered = records.stream()
                .sorted(Comparator.comparingLong(ZephyrExecutionRecord::startMillis)).toList();
        for (ZephyrExecutionRecord result : ordered) {
            String status = statusFor(result.outcome());
            String label = result.testCaseName();
            Optional<String> testCaseKey;
            try {
                testCaseKey = lookupFailed && !result.hasExplicitTestCaseKey() ? Optional.empty()
                        : resolveTestCaseKey(client, result, keysByName);
            } catch (ZephyrApiException problem) {
                failed++;
                lines.add(String.format(SUMMARY_LINE, "ERROR", "-", label) + "  <-- " + problem.getMessage());
                LOGGER.error("Zephyr: could not create a test case for '{}'. {}", label, problem.getMessage());
                continue;
            }
            if (testCaseKey.isEmpty()) {
                unmapped++;
                lines.add(String.format(SUMMARY_LINE, "NOT SENT", "-", label));
                continue;
            }
            try {
                postAutomatedExecution(client, testCycleKey, testCaseKey.get(), status, result, run, accountId);
                posted++;
                reportedKeys.add(testCaseKey.get().toUpperCase(Locale.ROOT));
                lines.add(String.format(SUMMARY_LINE, status, testCaseKey.get(), label));
            } catch (ZephyrApiException problem) {
                failed++;
                lines.add(String.format(SUMMARY_LINE, "ERROR", testCaseKey.get(), label)
                        + "  <-- " + problem.getMessage());
                LOGGER.error("Zephyr: execution for [{}] was rejected. {}", testCaseKey.get(), problem.getMessage());
            }
        }
        int manualAdded = 0;
        for (String manualKey : settings.manualTestCaseKeys()) {
            if (reportedKeys.contains(manualKey)) {
                continue;
            }
            if (settings.usesFixedTestCycle()) {
                try {
                    if (client.hasExecution(settings.projectKey(), testCycleKey, manualKey)) {
                        lines.add(String.format(SUMMARY_LINE, "(unchanged)", manualKey,
                                "(manual test case, already in the cycle)"));
                        continue;
                    }
                } catch (ZephyrApiException problem) {
                    LOGGER.warn("Zephyr: could not check whether [{}] is already in [{}], so it was left alone. {}",
                            manualKey, testCycleKey, problem.getMessage());
                    lines.add(String.format(SUMMARY_LINE, "(unchanged)", manualKey,
                            "(manual test case, could not check the cycle)"));
                    continue;
                }
            }
            try {
                postManualExecution(client, testCycleKey, manualKey);
                manualAdded++;
                lines.add(String.format(SUMMARY_LINE, settings.statusNotExecuted(), manualKey,
                        "(manual test case, run it by hand from the cycle)"));
            } catch (ZephyrApiException problem) {
                failed++;
                lines.add(String.format(SUMMARY_LINE, "ERROR", manualKey, "(manual test case)")
                        + "  <-- " + problem.getMessage());
                LOGGER.error("Zephyr: manual test case [{}] could not be added to [{}]. {}",
                        manualKey, testCycleKey, problem.getMessage());
            }
        }
        LOGGER.info(BANNER_LINE);
        LOGGER.info("ZEPHYR SYNC    : project {} | test cycle {} | {}", settings.projectKey(), testCycleKey,
                client.baseUrl());
        LOGGER.info("Executions     : {} automated sent, {} manual added, {} rejected, {} without a Zephyr test case",
                posted, manualAdded, failed, unmapped);
        lines.forEach(LOGGER::info);
        LOGGER.info(BANNER_LINE);
        return new SyncResult(testCycleKey, posted, manualAdded, failed, unmapped, "");
    }

    private Optional<String> resolveTestCaseKey(ZephyrClient client,
                                                ZephyrExecutionRecord result,
                                                Map<String, String> keysByName) {
        if (result.hasExplicitTestCaseKey()) {
            return Optional.of(result.explicitTestCaseKey());
        }
        String createName = truncate(result.testCaseName(), MAX_NAME_LENGTH);
        String normalisedName = ZephyrClient.normaliseName(createName);
        String byName = keysByName.get(normalisedName);
        if (byName != null) {
            return Optional.of(byName);
        }
        Optional<String> byTestCaseId = findByTestCaseIdPrefix(result.testCaseId(), keysByName);
        if (byTestCaseId.isPresent()) {
            LOGGER.info("Zephyr: {} matched to existing test case [{}] by its id", result.testCaseId(),
                    byTestCaseId.get());
            return byTestCaseId;
        }
        if (!settings.autoCreateTestCases()) {
            LOGGER.warn("Zephyr: no test case named '{}' in project [{}]; add @ZephyrTestCase(\"{}-T..\") to {} "
                            + "or set zephyr.auto.create.test.cases=true",
                    result.testCaseName(), settings.projectKey(), settings.projectKey(), result.qualifiedTestName());
            return Optional.empty();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(PROJECT_KEY, settings.projectKey());
        body.put("name", createName);
        body.put("objective", "Automated TestNG test <code>" + escapeHtml(result.qualifiedTestName()) + "</code>");
        body.put("labels", List.of(TEST_CASE_LABEL));
        String key = client.createTestCase(body);
        keysByName.put(normalisedName, key);
        LOGGER.info("Zephyr: created test case [{}] '{}'", key, result.testCaseName());
        return Optional.of(key);
    }

    private void postAutomatedExecution(ZephyrClient client,
                                        String testCycleKey,
                                        String testCaseKey,
                                        String status,
                                        ZephyrExecutionRecord result,
                                        ZephyrRunInfo run,
                                        String accountId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(PROJECT_KEY, settings.projectKey());
        body.put(TEST_CASE_KEY, testCaseKey);
        body.put(TEST_CYCLE_KEY, testCycleKey);
        body.put(STATUS_NAME, status);
        body.put("actualEndDate", isoSeconds(result.endMillis()));
        body.put("executionTime", result.durationMillis());
        body.put(COMMENT, executionComment(result, run));
        boolean hasOptionalFields = false;
        if (!optionalExecutionFieldsRejected) {
            if (settings.hasEnvironment()) {
                body.put(ENVIRONMENT_NAME, settings.environmentName());
                hasOptionalFields = true;
            }
            if (!accountId.isEmpty()) {
                body.put(EXECUTED_BY_ID, accountId);
                hasOptionalFields = true;
            }
        }
        try {
            client.createTestExecution(body);
        } catch (ZephyrApiException problem) {
            if (problem.statusCode() != HTTP_BAD_REQUEST || !hasOptionalFields) {
                throw problem;
            }
            optionalExecutionFieldsRejected = true;
            LOGGER.warn("Zephyr: execution rejected with environment/executed-by set, sending without them. "
                    + "Check zephyr.environment matches an environment in Zephyr. {}", problem.getMessage());
            body.remove(ENVIRONMENT_NAME);
            body.remove(EXECUTED_BY_ID);
            client.createTestExecution(body);
        }
    }

    private void postManualExecution(ZephyrClient client, String testCycleKey, String testCaseKey) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(PROJECT_KEY, settings.projectKey());
        body.put(TEST_CASE_KEY, testCaseKey);
        body.put(TEST_CYCLE_KEY, testCycleKey);
        body.put(STATUS_NAME, settings.statusNotExecuted());
        body.put(COMMENT, "Manual test case: not covered by the automated suite. Run it by hand from this cycle.");
        client.createTestExecution(body);
    }

    static Optional<String> findByTestCaseIdPrefix(String testCaseId, Map<String, String> keysByName) {
        if (testCaseId == null || testCaseId.isBlank()) {
            return Optional.empty();
        }
        String prefix = ZephyrClient.normaliseName(testCaseId);
        return keysByName.entrySet().stream().filter(entry -> startsWithId(entry.getKey(), prefix))
                .map(Map.Entry::getValue).findFirst();
    }

    private static boolean startsWithId(String normalisedName, String normalisedId) {
        if (!normalisedName.startsWith(normalisedId)) {
            return false;
        }
        if (normalisedName.length() == normalisedId.length()) {
            return true;
        }
        char next = normalisedName.charAt(normalisedId.length());
        return !Character.isLetterOrDigit(next) && next != '_';
    }

    private String statusFor(Outcome outcome) {
        return switch (outcome) {
            case PASSED -> settings.statusPass();
            case FAILED -> settings.statusFail();
            case SKIPPED -> settings.statusSkip();
        };
    }

    private static String cycleDescription(ZephyrRunInfo run, List<ZephyrExecutionRecord> records, int manualCount) {
        long passed = records.stream().filter(r -> r.outcome() == Outcome.PASSED).count();
        long failed = records.stream().filter(r -> r.outcome() == Outcome.FAILED).count();
        long skipped = records.stream().filter(r -> r.outcome() == Outcome.SKIPPED).count();
        List<String> lines = new ArrayList<>();
        lines.add("Automated run of TestNG suite " + run.suiteName());
        lines.add("Environment: " + run.environment() + " | Browser: " + run.browserLabel());
        lines.add("Automated results: " + passed + " passed, " + failed + " failed, " + skipped + " skipped");
        if (manualCount > 0) {
            lines.add("Manual test cases added as Not Executed: " + manualCount);
        }
        lines.add("Started: " + displayTime(run.startMillis()) + " | Finished: " + displayTime(run.endMillis()));
        lines.add("Machine: " + firstNonBlank(System.getenv("COMPUTERNAME"), System.getenv("HOSTNAME"), "unknown")
                + " | User: " + firstNonBlank(System.getProperty("user.name"), "unknown"));
        String buildUrl = System.getenv("BUILD_URL");
        if (buildUrl != null && !buildUrl.isBlank()) {
            lines.add("Jenkins build: " + buildUrl);
        }
        return joinEscaped(lines);
    }

    private static String executionComment(ZephyrExecutionRecord result, ZephyrRunInfo run) {
        List<String> lines = new ArrayList<>();
        lines.add("Automated run on " + run.browserLabel() + " | " + run.environment());
        lines.add("Test: " + result.qualifiedTestName()
                + (result.parameters().isEmpty() ? "" : " " + result.parameters()));
        if (result.retryCount() > 0) {
            lines.add("Retried " + result.retryCount() + " time(s) before this final result");
        }
        if (!result.failureMessage().isEmpty()) {
            String prefix = result.outcome() == Outcome.SKIPPED ? "Skip reason: " : "Failure: ";
            lines.add(prefix + truncate(result.failureMessage(), MAX_FAILURE_LENGTH));
        }
        return joinEscaped(lines);
    }

    private static String joinEscaped(List<String> lines) {
        return String.join(LINE_BREAK, lines.stream().map(ZephyrSyncService::escapeHtml).toList());
    }

    private static String isoSeconds(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private static String displayTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
                .format(CYCLE_TIMESTAMP);
    }

    private static String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                   .replace("\r\n", LINE_BREAK).replace("\n", LINE_BREAK);
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return "";
    }
}
