package com.enterprise.automation.utilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ZephyrTestCycle {
    String id();
}
