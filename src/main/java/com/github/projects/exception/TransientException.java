package com.github.projects.exception;

/**
 * Exception thrown when a transient error occurs, indicating it may be retried.
 */
public class TransientException extends RuntimeException {

    public TransientException(String message) {
        super(message);
    }

    public TransientException(String message, Throwable cause) {
        super(message, cause);
    }
}