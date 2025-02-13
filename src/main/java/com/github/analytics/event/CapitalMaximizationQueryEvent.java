package com.github.analytics.event;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.github.projects.model.Validators.*;

/**
 * Event representing the capital maximization process, to be published to a Kafka topic.
 *
 * @param maxProjects    The maximum number of projects to complete.
 * @param initialCapital The initial capital available for maximization.
 */
public record CapitalMaximizationQueryEvent(
        Integer maxProjects,
        BigDecimal initialCapital
) implements Serializable {
    public CapitalMaximizationQueryEvent {
        requireNonNull(maxProjects, () -> "Max projects should not be null");
        requireNonNegative(maxProjects, () -> "Initial capital should not be null");
        requireNonNullAndNonNegative(initialCapital, () -> "Initial capital must not be null and must be non-negative");
    }
}