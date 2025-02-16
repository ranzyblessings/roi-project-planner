package com.github.projects.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Data transfer object (DTO) for creating a new project.
 * Includes the project name, required capital to start the project, and the expected profit.
 */
public record CreateProjectRequest(
        @NotBlank(message = "Project name cannot be blank")
        @Size(max = 100, message = "Project name must not exceed 100 characters")
        String name,

        @NotNull(message = "The capital required to start the project cannot be null")
        @DecimalMin(value = "0.00", message = "Required capital cannot be negative")
        BigDecimal requiredCapital,

        @NotNull(message = "Expected project profit cannot be null")
        @DecimalMin(value = "0.00", message = "Profit cannot be negative")
        BigDecimal profit
) {
}