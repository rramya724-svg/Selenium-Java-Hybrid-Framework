package com.enterprise.automation.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Single entry point for obtaining Log4j2 loggers.
 *
 * <p>Centralising logger creation means the logging backend can be swapped in
 * exactly one place, and every class receives a correctly named logger without
 * repeating the {@code LogManager.getLogger} boilerplate.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public final class LoggerUtility {

    private LoggerUtility() {
        throw new IllegalStateException("LoggerUtility is a utility class and must not be instantiated");
    }

    /**
     * Creates a logger named after the supplied class.
     *
     * @param clazz the class requesting a logger
     * @return a configured Log4j2 logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }
}
