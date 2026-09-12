package com.enterprise.automation.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Applies {@link RetryAnalyzer} to every {@code @Test} method in the suite.
 *
 * <p>This removes the need to write {@code retryAnalyzer = RetryAnalyzer.class}
 * on hundreds of individual test methods, which is both DRY and impossible for
 * an engineer to forget. A method that already declares a real analyzer keeps
 * it, so bespoke retry policies remain possible.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class RetryTransformer implements IAnnotationTransformer {

    /** TestNG's placeholder analyzer, referenced by name to avoid depending on an internal class. */
    private static final String DISABLED_RETRY_ANALYZER_SIMPLE_NAME = "DisabledRetryAnalyzer";

    /**
     * Injects the default retry analyzer when a test method does not declare one.
     *
     * @param annotation      the annotation being transformed
     * @param testClass       the class under transformation, may be {@code null}
     * @param testConstructor the constructor under transformation, may be {@code null}
     * @param testMethod      the method under transformation, may be {@code null}
     */
    
    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {

        if (requiresDefaultAnalyzer(annotation.getRetryAnalyzerClass())) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }

    private boolean requiresDefaultAnalyzer(Class<? extends IRetryAnalyzer> declaredAnalyzer) {
        return declaredAnalyzer == null
                || DISABLED_RETRY_ANALYZER_SIMPLE_NAME.equals(declaredAnalyzer.getSimpleName());
    }
}
