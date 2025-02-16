package com.github.analytics.api;

import com.github.projects.model.ProjectDTO;

import java.math.BigDecimal;
import java.util.List;

import static com.github.projects.model.Validators.requireNonNull;
import static com.github.projects.model.Validators.requireNonNullAndNonNegative;

/**
 * Immutable record representing the result of a capital maximization operation.
 * Represents the outcome of capital maximization, including the selected projects and final capital.
 */
public record ProjectCapitalOptimized(
        List<ProjectDTO> selectedProjects,
        BigDecimal finalCapital) {

    public ProjectCapitalOptimized {
        requireNonNull(selectedProjects, () -> "Selected projects cannot be null.");
        requireNonNullAndNonNegative(finalCapital, () -> "Final capital cannot be null or negative.");
    }
}