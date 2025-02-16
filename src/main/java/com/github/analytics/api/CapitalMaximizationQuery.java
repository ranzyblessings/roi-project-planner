package com.github.analytics.api;

import com.github.projects.model.ProjectDTO;

import java.math.BigDecimal;
import java.util.List;

import static com.github.projects.model.Validators.*;

/**
 * Immutable record representing a query to maximize capital.
 * Encapsulates available projects, the maximum number of selections, and initial capital for optimization.
 */
public record CapitalMaximizationQuery(
        List<ProjectDTO> availableProjects,
        int maxProjects,
        BigDecimal initialCapital) {

    public CapitalMaximizationQuery {
        requireNonNullAndNoNullElements(availableProjects, () -> "Available projects list cannot be null or contain null elements.");
        requireNonNegative(maxProjects, () -> "Maximum projects must be zero or greater.");
        requireNonNullAndNonNegative(initialCapital, () -> "Initial capital cannot be null or negative.");
    }
}