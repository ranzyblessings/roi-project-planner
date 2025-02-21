package com.github.projects.exception;

/**
 * Exception thrown when a project-related validation fails,
 * such as null collections, empty lists, or invalid project attributes.
 */
public class InvalidProjectException extends RuntimeException {

    public InvalidProjectException(String message) {
        super(message);
    }

    public InvalidProjectException(String message, Throwable cause) {
        super(message, cause);
    }
}