package com.enterprise.automation.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryTransformer implements IAnnotationTransformer {
    private static final String DISABLED_RETRY_ANALYZER_SIMPLE_NAME = "DisabledRetryAnalyzer";

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (requiresDefaultAnalyzer(annotation.getRetryAnalyzerClass())) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }

    private boolean requiresDefaultAnalyzer(Class<? extends IRetryAnalyzer> declaredAnalyzer) {
        return declaredAnalyzer == null || DISABLED_RETRY_ANALYZER_SIMPLE_NAME.equals(declaredAnalyzer.getSimpleName());
    }
}
