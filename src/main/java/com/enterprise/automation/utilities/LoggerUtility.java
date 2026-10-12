package com.enterprise.automation.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LoggerUtility {
    private LoggerUtility() {
        throw new IllegalStateException("LoggerUtility is a utility class and must not be instantiated");
    }

    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }
}
