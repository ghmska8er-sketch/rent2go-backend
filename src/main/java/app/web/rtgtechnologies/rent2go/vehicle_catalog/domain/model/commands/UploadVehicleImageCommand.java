package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

/**
 * UploadVehicleImageCommand
 * 
 * Command to upload and attach an image to a vehicle.
 * 
 * CQRS Pattern: Modifying command that triggers a state change.
 */
public record UploadVehicleImageCommand(
    Long vehicleId,
    String imagePath,
    String imageUrl,
    Boolean isPrimary
) {
}
