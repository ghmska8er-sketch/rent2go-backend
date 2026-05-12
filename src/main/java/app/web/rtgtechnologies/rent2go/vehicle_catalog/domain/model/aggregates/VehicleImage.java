package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * VehicleImage Entity
 * 
 * Represents an image associated with a Vehicle.
 * Owned by Vehicle aggregate root.
 * 
 * Invariants:
 * - Each vehicle can have multiple images
 * - Only one image can be primary (isPrimary = true)
 * - Image path must not be empty
 * - uploadDate must be set on creation
 * 
 * DDD: This is an entity, not an aggregate root. It only exists in the context
 * of a Vehicle aggregate and should not be accessed directly through a repository.
 */
@Entity
@Table(name = "vehicle_images")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImage extends AuditableAbstractAggregateRoot<VehicleImage> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false, length = 500)
    private String imagePath;

    @Column(nullable = false)
    private Boolean isPrimary;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column(length = 255)
    private String imageUrl;

    // ========== Business Logic ==========

    /**
     * Mark this image as the primary image for its vehicle.
     * 
     * This method only changes the flag; the aggregate root should handle
     * ensuring only one primary image per vehicle.
     */
    public void markAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * Mark this image as non-primary.
     */
    public void unmarkAsPrimary() {
        this.isPrimary = false;
    }

    /**
     * Get the upload date of this image.
     */
    public LocalDateTime getUploadedDate() {
        return this.uploadDate;
    }

    /**
     * Check if this image is primary.
     */
    public boolean isPrimary() {
        return Boolean.TRUE.equals(this.isPrimary);
    }

    /**
     * Set the vehicle reference for this image (used by aggregate root).
     */
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
