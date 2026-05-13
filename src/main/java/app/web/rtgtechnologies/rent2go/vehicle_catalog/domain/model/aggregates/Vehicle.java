package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Vehicle Aggregate Root
 * 
 * Core entity for the vehicle-catalog bounded context.
 * Represents a vehicle available for rental in the Rent2Go platform.
 * 
 * Invariants:
 * - license_plate must be unique
 * - daily_price must be > 0
 * - status must be one of the valid VehicleStatus values
 * - vehicle must have a category
 */
@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends AuditableAbstractAggregateRoot<Vehicle> {

    @Column(unique = true, nullable = false, length = 20)
    private String licensePlate;

    @Column(nullable = false, length = 50)
    private String make;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(unique = true, nullable = false, length = 50)
    private String vin;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private VehicleCategory category;

    @Column(length = 100)
    private String location;

    @Column(name = "primary_image_path", length = 500)
    private String primaryImagePath;

    @Column(name = "primary_image_url", length = 255)
    private String primaryImageUrl;

    @Column(length = 500)
    private String description;

    @Column(name = "seats", nullable = false)
    private Integer seats;

    @Column(name = "transmission", length = 20)
    private String transmission;

    @Column(name = "fuel_type", length = 20)
    private String fuelType;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "vehicle")
    private List<VehicleImage> images = new ArrayList<>();

    // ========== Business Logic ==========

    public boolean isAvailable() {
        return VehicleStatus.AVAILABLE == this.status;
    }

    public void makeAvailable() {
        if (this.status != VehicleStatus.RENTED && this.status != VehicleStatus.MAINTENANCE) {
            this.status = VehicleStatus.AVAILABLE;
        }
    }

    public void markAsRented() {
        if (this.isAvailable()) {
            this.status = VehicleStatus.RENTED;
        } else {
            throw new IllegalStateException("Vehicle must be available to rent");
        }
    }

    public void markAsMaintenanceRequired() {
        this.status = VehicleStatus.MAINTENANCE;
    }

    public void updateDailyPrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.dailyPrice = newPrice;
    }

    /**
     * Update vehicle profile details.
     */
    public void updateDetails(VehicleCategory category, String make, String model, Integer year,
                              String location, String description, Integer seats,
                              String transmission, String fuelType) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        this.category = category;
        this.make = make;
        this.model = model;
        this.year = year;
        this.location = location;
        this.description = description;
        this.seats = seats;
        this.transmission = transmission;
        this.fuelType = fuelType;
    }

    public void retire() {
        this.status = VehicleStatus.RETIRED;
    }

    /**
     * Add an image to this vehicle.
     * 
     * If isPrimary is true and there's already a primary image, 
     * the existing primary is unmarked first.
     * 
     * @param image VehicleImage to add
     */
    public void addImage(VehicleImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        boolean shouldBePrimary = image.isPrimary() || this.images.isEmpty();

        if (shouldBePrimary) {
            this.images.stream()
                .filter(VehicleImage::isPrimary)
                .forEach(VehicleImage::unmarkAsPrimary);
            image.markAsPrimary();
        }
        
        // Set bidirectional relationship
        image.setVehicle(this);
        this.images.add(image);

        if (shouldBePrimary) {
            this.primaryImagePath = image.getImagePath();
            this.primaryImageUrl = image.getImageUrl();
        }
    }

    /**
     * Remove an image from this vehicle by its ID.
     * 
     * @param imageId Image ID to remove
     * @return true if removed, false if not found
     */
    public boolean removeImage(Long imageId) {
        VehicleImage removed = this.images.stream()
            .filter(img -> img.getId() != null && img.getId().equals(imageId))
            .findFirst()
            .orElse(null);

        if (removed == null) {
            return false;
        }

        boolean wasPrimary = removed.isPrimary();
        this.images.remove(removed);

        if (wasPrimary) {
            VehicleImage nextPrimary = this.images.stream().findFirst().orElse(null);
            if (nextPrimary != null) {
                nextPrimary.markAsPrimary();
                this.primaryImagePath = nextPrimary.getImagePath();
                this.primaryImageUrl = nextPrimary.getImageUrl();
            } else {
                this.primaryImagePath = null;
                this.primaryImageUrl = null;
            }
        }

        return true;
    }

    /**
     * Mark an existing image as the primary image.
     */
    public void setPrimaryImage(Long imageId) {
        VehicleImage selected = this.images.stream()
            .filter(img -> img.getId() != null && img.getId().equals(imageId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        this.images.forEach(VehicleImage::unmarkAsPrimary);
        selected.markAsPrimary();
        this.primaryImagePath = selected.getImagePath();
        this.primaryImageUrl = selected.getImageUrl();
    }

    /**
     * Get the primary image for this vehicle, if any.
     * 
     * @return Primary VehicleImage or null if none marked as primary
     */
    public VehicleImage getPrimaryImage() {
        return this.images.stream()
            .filter(VehicleImage::isPrimary)
            .findFirst()
            .orElse(null);
    }

    /**
     * Get all images for this vehicle.
     * 
     * @return List of VehicleImage entities (may be empty)
     */
    public List<VehicleImage> getImages() {
        return new ArrayList<>(this.images);
    }

    /**
     * Check if this vehicle has any images.
     * 
     * @return true if vehicle has images, false otherwise
     */
    public boolean hasImages() {
        return !this.images.isEmpty();
    }
}
