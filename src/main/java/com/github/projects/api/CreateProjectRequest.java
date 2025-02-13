package com.github.projects.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Data transfer object (DTO) for creating a new project.
 * Includes the project name, required capital to start the project, and the expected profit.
 */
public record CreateProjectRequest(
        @NotBlank(message = "Project name cannot be blank")
        String name,

        @NotNull(message = "The capital required to start the project cannot be null")
        @DecimalMin(value = "0.00", message = "Required capital must be greater than or equal to 0")
        BigDecimal requiredCapital,

        @NotNull(message = "Expected project profit cannot be null")
        @DecimalMin(value = "0.00", message = "Profit must be greater than or equal to 0")
        BigDecimal profit
) {
}