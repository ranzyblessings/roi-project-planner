package com.github.analytics;

import com.github.projects.model.ProjectDTO;

import java.math.BigDecimal;
import java.util.List;

import static com.github.projects.model.Validators.*;

/**
 * Immutable record representing a query to maximize capital.
 * Includes the list of available projects, the maximum number of projects to complete, and the initial capital.
 */
public record CapitalMaximizationQuery(
        List<ProjectDTO> availableProjects,
        int maxProjects,
        BigDecimal initialCapital) {

    public CapitalMaximizationQuery {
        requireNonNullAndNoNullElements(availableProjects, () -> "Available projects must not be null nor contain null elements");
        requireNonNegative(maxProjects, () -> "Max projects must be non-negative");
        requireNonNullAndNonNegative(initialCapital, () -> "Initial capital must not be null and must be non-negative");
    }
}