package com.enterprise.mobile.exceptions;

/**
 * Base unchecked exception for framework level failures (configuration,
 * driver creation, device allocation, ...).
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
