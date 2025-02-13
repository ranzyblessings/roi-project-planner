package com.github.analytics.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Data transfer object (DTO) for requesting capital optimization among a pool of projects.
 * Includes the maximum number of projects to complete and the initial capital available.
 * In the future, this will support more advanced project selection based on various criteria.
 */
public record ProjectCapitalOptimizerRequest(
        @NotNull(message = "Max projects cannot be null")
        @Positive(message = "Max projects must be greater than zero")
        Integer maxProjects,

        @NotNull(message = "Initial capital cannot be null")
        @DecimalMin(value = "0.00", message = "Initial capital must be greater than or equal to 0")
        BigDecimal initialCapital
) {

}