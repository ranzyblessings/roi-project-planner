package com.github.projects.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import java.io.Serializable;
import java.time.Instant;

/**
 * Tracks creation and modification timestamps for entity auditing.
 * Uses Spring Data’s auditing framework to autopopulate timestamps.
 */
@UserDefinedType("audit_metadata")
public record AuditMetadata(
        @CreatedDate @Column("created_at") Instant createdAt,
        @LastModifiedDate @Column("updated_at") Instant updatedAt
) implements Serializable {

    /**
     * Returns an instance with null timestamps.
     * Spring Data will populate these values automatically.
     */
    public static AuditMetadata empty() {
        return new AuditMetadata(null, null);
    }
}
