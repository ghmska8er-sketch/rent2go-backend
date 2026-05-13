package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

import java.math.BigDecimal;

/**
 * UpdateVehicleDetailsCommand
 *
 * Command to update a vehicle's editable details.
 */
public record UpdateVehicleDetailsCommand(
    Long vehicleId,
    Long categoryId,
    String make,
    String model,
    Integer year,
    String location,
    String description,
    Integer seats,
    String transmission,
    String fuelType
) {
}
