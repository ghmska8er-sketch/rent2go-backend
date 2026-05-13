package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

/**
 * SetPrimaryVehicleImageCommand
 *
 * Command to mark an existing vehicle image as the primary image.
 */
public record SetPrimaryVehicleImageCommand(
    Long vehicleId,
    Long imageId
) {
}
