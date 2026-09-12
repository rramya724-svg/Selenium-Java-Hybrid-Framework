package com.enterprise.automation.exceptions;

import java.io.Serial;

/**
 * Single unchecked exception type raised by the framework.
 *
 * <p>Wrapping low level checked exceptions in one runtime type keeps test code
 * free of {@code try/catch} noise while still preserving the original cause for
 * diagnostics and reporting.</p>
 *
 * @author Lalith Kumar BV
 * @version 1.0
 */
public class FrameworkException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a diagnostic message.
     *
     * @param message description of what failed
     */
    public FrameworkException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a diagnostic message and the originating cause.
     *
     * @param message description of what failed
     * @param cause   the underlying exception
     */
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
