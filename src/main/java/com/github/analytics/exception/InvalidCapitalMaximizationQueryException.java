package com.github.analytics.exception;

import com.github.analytics.api.CapitalMaximizationQuery;

/**
 * Exception thrown when an invalid {@link CapitalMaximizationQuery} is provided.
 * This typically occurs when the query itself is null or contains invalid data,
 * such as a null or empty list of available projects.
 */
public class InvalidCapitalMaximizationQueryException extends RuntimeException {

    public InvalidCapitalMaximizationQueryException(String message) {
        super(message);
    }

    public InvalidCapitalMaximizationQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}