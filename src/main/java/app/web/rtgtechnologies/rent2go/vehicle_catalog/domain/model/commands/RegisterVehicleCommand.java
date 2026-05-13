package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

import java.math.BigDecimal;

/**
 * RegisterVehicleCommand
 * 
 * Command to register a new vehicle in the catalog.
 * 
 * CQRS Pattern: Modifying command that triggers a state change.
 */
public record RegisterVehicleCommand(
    String licensePlate,
    String make,
    String model,
    Integer year,
    String vin,
    BigDecimal dailyPrice,
    Long categoryId,
    String location,
    String description,
    Integer seats,
    String transmission,
    String fuelType
) {
}
