package com.github.analytics;

import com.github.projects.model.ProjectDTO;

import java.math.BigDecimal;
import java.util.List;

import static com.github.projects.model.Validators.requireNonNull;
import static com.github.projects.model.Validators.requireNonNullAndNonNegative;

/**
 * Immutable record representing the result of a capital maximization operation.
 * Contains the selected projects and the final accumulated capital.
 */
public record ProjectCapitalOptimized(
        List<ProjectDTO> selectedProjects,
        BigDecimal finalCapital) {

    public ProjectCapitalOptimized {
        requireNonNull(selectedProjects, () -> "Selected projects list must not be null");
        requireNonNullAndNonNegative(finalCapital, () -> "Final capital must not be null and must be non-negative");
    }
}