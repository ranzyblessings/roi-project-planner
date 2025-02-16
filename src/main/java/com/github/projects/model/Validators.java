package com.github.projects.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Provides centralized and reusable validation utilities.
 */
public final class Validators {

    private Validators() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates the provided string is neither null nor blank.
     *
     * @param value        The string to validate.
     * @param errorMessage Supplier for the error message if validation fails.
     * @throws IllegalArgumentException If the string is null or blank.
     */
    public static void requireNonNullOrBlank(String value, Supplier<String> errorMessage) {
        Objects.requireNonNull(value, errorMessage.get());
        if (value.isBlank()) {
            throw new IllegalArgumentException(errorMessage.get());
        }
    }

    /**
     * Validates the provided BigDecimal is non-null and non-negative.
     *
     * @param value        The BigDecimal to validate.
     * @param errorMessage Supplier for the error message if validation fails.
     * @throws IllegalArgumentException If the value is null or negative.
     */
    public static void requireNonNullAndNonNegative(BigDecimal value, Supplier<String> errorMessage) {
        Objects.requireNonNull(value, errorMessage.get());
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(errorMessage.get());
        }
    }

    /**
     * Validates the collection is non-null, non-empty, and contains no null elements.
     *
     * @param collection   The collection to validate.
     * @param errorMessage Supplier for the error message if validation fails.
     * @param <T>          The type of elements in the collection.
     * @throws IllegalArgumentException If the collection is null, empty, or contains null elements.
     */
    public static <T> void requireNonNullAndNoNullElements(Collection<T> collection, Supplier<String> errorMessage) {
        Objects.requireNonNull(collection, errorMessage.get());
        if (collection.isEmpty() || collection.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(errorMessage.get());
        }
    }

    /**
     * Validates the provided integer is non-negative.
     *
     * @param value        The integer to validate.
     * @param errorMessage Supplier for the error message if validation fails.
     * @throws IllegalArgumentException If the value is negative.
     */
    public static void requireNonNegative(int value, Supplier<String> errorMessage) {
        if (value < 0) {
            throw new IllegalArgumentException(errorMessage.get());
        }
    }

    /**
     * Validates the provided value is non-null.
     *
     * @param value        The value to validate.
     * @param errorMessage Supplier for the error message if validation fails.
     * @throws IllegalArgumentException If the value is null.
     */
    public static <T> void requireNonNull(T value, Supplier<String> errorMessage) {
        try {
            Objects.requireNonNull(value);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(errorMessage.get());
        }
    }
}