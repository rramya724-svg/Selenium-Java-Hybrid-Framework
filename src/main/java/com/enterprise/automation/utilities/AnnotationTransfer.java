package com.enterprise.automation.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class AnnotationTransfer implements IAnnotationTransformer {

	public synchronized void transform(ITestAnnotation testAnnotation, @SuppressWarnings("rawtypes") Class testClass, @SuppressWarnings("rawtypes") Constructor testConstructor,
			Method testMethod) {
		testAnnotation.setRetryAnalyzer(RetryAnalyzer.class);
	}

}
