package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects;

/**
 * Vehicle Status Enum
 * 
 * Represents the status of a vehicle in the catalog.
 * - AVAILABLE: ready for rental
 * - RENTED: currently booked
 * - MAINTENANCE: under maintenance
 * - RETIRED: no longer available for rental
 */
public enum VehicleStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE,
    RETIRED;

    public boolean isAvailable() {
        return this == AVAILABLE;
    }
}
