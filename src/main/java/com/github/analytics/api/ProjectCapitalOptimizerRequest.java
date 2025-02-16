package com.github.analytics.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for requesting capital optimization among a pool of projects.
 * Specifies the maximum number of projects that can be selected and the available initial capital.
 *
 * <p> Future enhancements will introduce advanced project selection based on additional criteria. </p>
 */
public record ProjectCapitalOptimizerRequest(
        @NotNull(message = "Maximum projects cannot be null")
        @Positive(message = "Maximum projects must be at least 1")
        Integer maxProjects,

        @NotNull(message = "Initial capital cannot be null")
        @DecimalMin(value = "0.00", message = "Initial capital cannot be negative")
        BigDecimal initialCapital
) {

}