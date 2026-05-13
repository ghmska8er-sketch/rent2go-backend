package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

import java.math.BigDecimal;

/**
 * UpdateVehiclePricingCommand
 * 
 * Command to update the daily price of a vehicle.
 */
public record UpdateVehiclePricingCommand(
    Long vehicleId,
    BigDecimal newDailyPrice
) {
}
