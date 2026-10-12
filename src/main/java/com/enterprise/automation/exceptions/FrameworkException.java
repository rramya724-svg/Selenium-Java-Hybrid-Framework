package com.enterprise.automation.exceptions;

import java.io.Serial;

public class FrameworkException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
