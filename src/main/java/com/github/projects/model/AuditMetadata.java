package com.github.projects.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents audit metadata for tracking creation and modification timestamps.
 * Integrates with Spring Data’s auditing framework to manage these timestamps automatically.
 */
@UserDefinedType("audit_metadata")
public record AuditMetadata(
        @CreatedDate @Column("created_at") Instant createdAt,
        @LastModifiedDate @Column("updated_at") Instant updatedAt
) implements Serializable {

    /**
     * Returns an {@code AuditMetadata} instance with null timestamps,
     * which will be automatically set and updated by Spring Data's auditing system.
     */
    public static AuditMetadata empty() {
        return new AuditMetadata(null, null);
    }
}