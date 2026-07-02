package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.VehiclePerformanceResource;

import java.time.LocalDate;

/**
 * VehiclePerformanceService
 *
 * US24: Business logic contract for computing per-vehicle performance
 * metrics (reservation count, revenue sum, occupancy percentage) within
 * an optional date range.
 */
public interface VehiclePerformanceService {

    /**
     * Compute performance metrics for a single vehicle.
     *
     * @param vehicleId the vehicle to compute metrics for
     * @param from      inclusive start date of the reporting period (nullable)
     * @param to        inclusive end date of the reporting period (nullable)
     * @return a fully populated VehiclePerformanceResource
     * @throws IllegalArgumentException if the vehicle does not exist
     */
    VehiclePerformanceResource getPerformance(Long vehicleId, LocalDate from, LocalDate to);
}
