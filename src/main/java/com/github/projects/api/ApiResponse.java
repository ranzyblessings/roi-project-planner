package com.github.projects.api;

/**
 * Standardized API response wrapper providing a consistent structure.
 * Encapsulates the HTTP status code, message, and data.
 * Immutable and thread-safe.
 *
 * @param <T> The response payload type.
 */
public final class ApiResponse<T> {
    private final int statusCode;
    private final String message;
    private final T data;

    private ApiResponse(int statusCode, String message, T data) {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("Invalid HTTP status code: %d".formatted(statusCode));
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
     * Creates a success response with the given status code and data.
     *
     * @param statusCode HTTP status code.
     * @param data       Response payload.
     * @param <T>        Type of response data.
     * @return A success response instance.
     * @throws IllegalArgumentException if data is null.
     */
    public static <T> ApiResponse<T> success(int statusCode, T data) {
        if (data == null) {
            throw new IllegalArgumentException("Success response data cannot be null");
        }
        return new ApiResponse<>(statusCode, null, data);
    }

    /**
     * Creates an error response with the given status code and message.
     *
     * @param statusCode HTTP status code.
     * @param message    Error message.
     * @param <T>        Response data type (typically null for errors).
     * @return An error response instance.
     * @throws IllegalArgumentException if message is null or empty.
     */
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Error response message cannot be null or empty");
        }
        return new ApiResponse<>(statusCode, message, null);
    }

    /**
     * Returns a formatted string representation of the response.
     *
     * @return A string containing status code, message, and data.
     */
    @Override
    public String toString() {
        return "ApiResponse{statusCode=%d, message='%s', data=%s}".formatted(statusCode, message, data);
    }
}