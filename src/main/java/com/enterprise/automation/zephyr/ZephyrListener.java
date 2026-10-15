package com.enterprise.automation.zephyr;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.utilities.ExcelUtility;
import com.enterprise.automation.utilities.LoggerUtility;
import com.enterprise.automation.utilities.TestDataRow;
import com.enterprise.automation.zephyr.ZephyrExecutionRecord.Outcome;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ZephyrListener implements ISuiteListener {
    private static final Logger LOGGER = LoggerUtility.getLogger(ZephyrListener.class);
    private static final String TEST_CASE_ID_FIELD = "TEST_CASE_ID";
    private static final String TEST_CASE_ID_ATTRIBUTE = "testCaseId";
    private static final Set<ISuite> REPORTED_SUITES =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
    private static final Map<String, Long> SUITE_START_MILLIS = new ConcurrentHashMap<>();

    @Override
    public void onStart(ISuite suite) {
        SUITE_START_MILLIS.put(suite.getName(), System.currentTimeMillis());
    }

    @Override
    public void onFinish(ISuite suite) {
        reportOnce(suite);
    }

    public static void reportOnce(ISuite suite) {
        long endMillis = System.currentTimeMillis();
        if (!REPORTED_SUITES.add(suite)) {
            LOGGER.debug("Zephyr: suite [{}] already reported", suite.getName());
            return;
        }
        LOGGER.info("Zephyr: suite [{}] finished, sending results to Zephyr", suite.getName());
        try {
            ZephyrSettings settings = ZephyrSettings.load();
            if (!settings.enabled()) {
                LOGGER.info("Zephyr: reporting is switched off (zephyr.enabled=false)");
                return;
            }
            LOGGER.info("Zephyr: reporting results of suite [{}] using {}", suite.getName(), settings);
            List<ZephyrExecutionRecord> records = collectFinalResults(suite);
            ZephyrRunInfo run = new ZephyrRunInfo(
                    suite.getName(),
                    SUITE_START_MILLIS.getOrDefault(suite.getName(), earliestStartMillis(records, endMillis)),
                    endMillis,
                    ConfigReader.get(ConfigKey.BROWSER, "chrome"),
                    ConfigReader.getBoolean(ConfigKey.HEADLESS, false),
                    ConfigReader.get(ConfigKey.ENVIRONMENT, "QA"));
            new ZephyrSyncService(settings).sync(run, records);
        } catch (RuntimeException | LinkageError unexpected) {
            LOGGER.error("Zephyr: unexpected problem while reporting results; test results are unaffected", unexpected);
        }
    }

    private static long earliestStartMillis(List<ZephyrExecutionRecord> records, long fallbackMillis) {
        return records.stream().mapToLong(ZephyrExecutionRecord::startMillis).filter(start -> start > 0)
                .min().orElse(fallbackMillis);
    }

    static List<ZephyrExecutionRecord> collectFinalResults(ISuite suite) {
        List<ITestResult> passed = new ArrayList<>();
        List<ITestResult> failed = new ArrayList<>();
        List<ITestResult> skipped = new ArrayList<>();
        for (ISuiteResult suiteResult : suite.getResults().values()) {
            ITestContext context = suiteResult.getTestContext();
            passed.addAll(context.getPassedTests().getAllResults());
            passed.addAll(context.getFailedButWithinSuccessPercentageTests().getAllResults());
            failed.addAll(context.getFailedTests().getAllResults());
            skipped.addAll(context.getSkippedTests().getAllResults());
        }
        Map<String, Integer> retriesByInvocation = new HashMap<>();
        for (ITestResult result : skipped) {
            if (result.wasRetried()) {
                retriesByInvocation.merge(invocationKey(result), 1, Integer::sum);
            }
        }
        List<ZephyrExecutionRecord> records = new ArrayList<>();
        addRecords(records, passed, Outcome.PASSED, retriesByInvocation);
        addRecords(records, failed, Outcome.FAILED, retriesByInvocation);
        addRecords(records, skipped.stream().filter(result -> !result.wasRetried()).toList(),
                Outcome.SKIPPED, retriesByInvocation);
        LOGGER.info("Zephyr: {} final result(s) collected ({} retried attempt(s) ignored)",
                records.size(), retriesByInvocation.values().stream().mapToInt(Integer::intValue).sum());
        return records;
    }

    private static void addRecords(List<ZephyrExecutionRecord> records,
                                   Collection<ITestResult> results,
                                   Outcome outcome,
                                   Map<String, Integer> retriesByInvocation) {
        for (ITestResult result : results) {
            records.add(toRecord(result, outcome, retriesByInvocation.getOrDefault(invocationKey(result), 0)));
        }
    }

    private static ZephyrExecutionRecord toRecord(ITestResult result, Outcome outcome, int retryCount) {
        ITestNGMethod testMethod = result.getMethod();
        Class<?> testClass = testMethod.getRealClass();
        String methodName = testMethod.getMethodName();
        Throwable throwable = result.getThrowable();
        String failureMessage = throwable == null ? "" : throwable.getClass().getSimpleName()
                  + (throwable.getMessage() == null ? "" : ": " + throwable.getMessage());
        String testCaseId = testCaseId(result, testClass);
        return new ZephyrExecutionRecord(
                testClass.getName() + "#" + methodName,
                testCaseId,
                testCaseName(result, testCaseId, testClass, methodName),
                explicitTestCaseKey(testMethod),
                outcome,
                result.getStartMillis(),
                result.getEndMillis(),
                retryCount,
                failureMessage,
                parametersOf(result));
    }

    private static String testCaseId(ITestResult result, Class<?> testClass) {
        String testCaseId = staticTestCaseId(testClass);
        if (testCaseId.isEmpty()) {
            Object attribute = result.getAttribute(TEST_CASE_ID_ATTRIBUTE);
            testCaseId = attribute == null ? "" : String.valueOf(attribute).trim();
        }
        return testCaseId;
    }

    private static String testCaseName(ITestResult result, String testCaseId, Class<?> testClass, String methodName) {
        String title = excelTestCaseName(testCaseId);
        if (title.isEmpty()) {
            String description = result.getMethod().getDescription();
            title = description == null || description.isBlank() ? testClass.getSimpleName() + "." + methodName
                    : description.trim();
        }
        return testCaseId.isEmpty() ? title : testCaseId + " - " + title;
    }

    private static String excelTestCaseName(String testCaseId) {
        if (testCaseId.isEmpty()) {
            return "";
        }
        try {
            return ExcelUtility.findByTestCaseId(testCaseId).map(TestDataRow::testCaseName).orElse("").trim();
        } catch (RuntimeException | LinkageError unreadable) {
            LOGGER.debug("Zephyr: no Excel test case name for [{}]: {}", testCaseId, unreadable.getMessage());
            return "";
        }
    }

    private static String staticTestCaseId(Class<?> testClass) {
        try {
            Field field = testClass.getDeclaredField(TEST_CASE_ID_FIELD);
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != String.class) {
                return "";
            }
            field.setAccessible(true);
            return Objects.toString(field.get(null), "").trim();
        } catch (ReflectiveOperationException | RuntimeException notAvailable) {
            return "";
        }
    }

    private static String explicitTestCaseKey(ITestNGMethod testMethod) {
        Method method = testMethod.getConstructorOrMethod().getMethod();
        if (method == null) {
            return "";
        }
        ZephyrTestCase annotation = method.getAnnotation(ZephyrTestCase.class);
        return annotation == null ? "" : annotation.value();
    }

    private static String parametersOf(ITestResult result) {
        Object[] parameters = result.getParameters();
        return parameters == null || parameters.length == 0 ? "" : Arrays.deepToString(parameters);
    }

    private static String invocationKey(ITestResult result) {
        return result.getMethod().getQualifiedName() + parametersOf(result);
    }
}
