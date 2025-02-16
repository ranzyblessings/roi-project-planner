package com.github.projects.model;

import org.springframework.data.annotation.Version;
import org.springframework.data.cassandra.core.mapping.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static com.github.projects.model.Validators.requireNonNullAndNonNegative;
import static com.github.projects.model.Validators.requireNonNullOrBlank;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

/**
 * Immutable record representing a project with a unique name, required capital, and expected profit.
 *
 * <p> The project name is indexed for optimized lookups, and versioning is used for optimistic locking. </p>
 */
@Table("projects")
public record ProjectEntity(
        @PrimaryKey UUID id,
        @Indexed String name,
        @CassandraType(type = CassandraType.Name.DECIMAL) @Column("required_capital") BigDecimal requiredCapital,
        @CassandraType(type = CassandraType.Name.DECIMAL) BigDecimal profit,
        AuditMetadata auditMetadata,
        @Version Long version
) {

    public ProjectEntity {
        // Validate string fields
        requireNonNullOrBlank(name, () -> "Project name must not be null or blank");

        // Validate numeric fields
        requireNonNullAndNonNegative(requiredCapital, () -> "Required capital must be non-null and non-negative");
        requireNonNullAndNonNegative(profit, () -> "Profit must be non-null and non-negative");

        // Validate audit metadata
        requireNonNull(auditMetadata, "Audit metadata must not be null");
    }

    /**
     * Creates a new {@code ProjectEntity} with a generated ID, empty audit metadata, and null version.
     * Spring Data populates these upon persistence.
     */
    public static ProjectEntity createNewProject(String name, BigDecimal requiredCapital, BigDecimal profit) {
        return new ProjectEntity(randomUUID(), name, requiredCapital, profit, AuditMetadata.empty(), null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProjectEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}