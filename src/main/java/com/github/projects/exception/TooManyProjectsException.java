package com.github.projects.exception;

/**
 * Exception thrown when attempting to create more projects than the allowed limit.
 */
public class TooManyProjectsException extends RuntimeException {

    public TooManyProjectsException(String message) {
        super(message);
    }

    public TooManyProjectsException(String message, Throwable cause) {
        super(message, cause);
    }
}