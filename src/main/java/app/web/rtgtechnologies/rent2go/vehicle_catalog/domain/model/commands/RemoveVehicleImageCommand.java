package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

/**
 * RemoveVehicleImageCommand
 * 
 * Command to remove an image from a vehicle.
 * 
 * CQRS Pattern: Modifying command that triggers a state change.
 */
public record RemoveVehicleImageCommand(
    Long vehicleId,
    Long imageId
) {
}
