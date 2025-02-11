package com.github.projects.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static com.github.projects.model.Validators.requireNonNullAndNonNegative;
import static com.github.projects.model.Validators.requireNonNullOrBlank;
import static java.util.Objects.requireNonNull;

public record ProjectDTO(
        UUID id,
        String name,
        BigDecimal requiredCapital,
        BigDecimal profit,
        AuditMetadata auditMetadata,
        Long version
) implements Serializable {

    public ProjectDTO {
        // Validate identifier fields
        requireNonNull(id, "Project ID must not be null or blank");
        requireNonNullOrBlank(name, () -> "Project name must not be null or blank");

        // Validate numeric fields
        requireNonNullAndNonNegative(requiredCapital, () -> "Required capital must not be null and must be non-negative");
        requireNonNullAndNonNegative(profit, () -> "Profit must not be null and must be non-negative");

        // Validate audit metadata and version
        requireNonNull(auditMetadata, "Audit metadata must not be null");
        requireNonNull(version, "Project version must not be null");
    }

    public static ProjectDTO fromEntity(ProjectEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Project entity cannot be null");
        }
        return new ProjectDTO(
                entity.id(),
                entity.name(),
                entity.requiredCapital(),
                entity.profit(),
                entity.auditMetadata(),
                entity.version());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProjectDTO that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}