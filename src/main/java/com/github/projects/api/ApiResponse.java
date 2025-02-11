package com.github.projects.api;

/**
 * Standardized API response wrapper for consistent structure. Includes HTTP status, message, and data.
 * Immutable and thread-safe.
 *
 * @param <T> The data type of the response payload.
 */
public final class ApiResponse<T> {
    private final int statusCode;
    private final String message;
    private final T data;

    private ApiResponse(int statusCode, String message, T data) {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException(String.format("Invalid HTTP status code: %d", statusCode));
        }
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    /**
     * Creates a success response with the provided HTTP status code and data.
     */
    public static <T> ApiResponse<T> success(int statusCode, T data) {
        if (data == null) {
            throw new IllegalArgumentException("Success response data cannot be null");
        }
        return new ApiResponse<>(statusCode, null, data);
    }

    /**
     * Creates an error response with the provided HTTP status code and the error message.
     */
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Error response message cannot be null or empty");
        }
        return new ApiResponse<>(statusCode, message, null);
    }

    @Override
    public String toString() {
        return "ApiResponse{statusCode=%d, message='%s', data=%s}".formatted(statusCode, message, data);
    }
}