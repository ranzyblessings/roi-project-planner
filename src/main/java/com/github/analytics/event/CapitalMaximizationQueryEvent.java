package com.github.analytics.event;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.github.projects.model.Validators.*;

/**
 * Represents a capital maximization event published to a Kafka topic.
 *
 * @param maxProjects    The maximum number of projects to complete.
 * @param initialCapital The initial capital available for maximization.
 */
public record CapitalMaximizationQueryEvent(
        Integer maxProjects,
        BigDecimal initialCapital
) implements Serializable {

    public CapitalMaximizationQueryEvent {
        requireNonNull(maxProjects, () -> "Max projects cannot be null.");
        requireNonNegative(maxProjects, () -> "Max projects must be zero or greater.");
        requireNonNullAndNonNegative(initialCapital, () -> "Initial capital cannot be null or negative.");
    }
}