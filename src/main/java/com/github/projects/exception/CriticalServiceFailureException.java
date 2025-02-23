package com.github.projects.exception;

/**
 * Exception thrown when a critical service failure occurs, indicating an unrecoverable error.
 */
public class CriticalServiceFailureException extends RuntimeException {

    public CriticalServiceFailureException(String message) {
        super(message);
    }

    public CriticalServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}