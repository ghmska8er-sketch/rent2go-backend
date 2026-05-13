package app.web.rtgtechnologies.rent2go.shared.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.Instant;

/**
 * Base Aggregate Root for DDD
 * 
 * Provides common functionality for all domain aggregates:
 * - Identity management (ID)
 * - Audit tracking (createdAt, updatedAt)
 * - Domain event support
 * 
 * All aggregates should extend this class to ensure consistent identity and lifecycle management.
 */
@Getter
@MappedSuperclass
public abstract class BaseAggregateRoot<T extends Serializable> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private T id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
