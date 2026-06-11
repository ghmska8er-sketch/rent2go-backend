package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

/**
 * UploadVehicleImageCommand
 * 
 * Command to upload and attach an image to a vehicle.
 * Either imagePath or imageUrl must be provided (imageUrl is typically set by Cloudinary).
 * 
 * CQRS Pattern: Modifying command that triggers a state change.
 */
public record UploadVehicleImageCommand(
    Long vehicleId,
    String imagePath,
    String imageUrl,
    Boolean isPrimary
) {
    public UploadVehicleImageCommand {
        if ((imagePath == null || imagePath.isBlank()) && (imageUrl == null || imageUrl.isBlank())) {
            throw new IllegalArgumentException("At least one of imagePath or imageUrl must be provided");
        }
    }
}
