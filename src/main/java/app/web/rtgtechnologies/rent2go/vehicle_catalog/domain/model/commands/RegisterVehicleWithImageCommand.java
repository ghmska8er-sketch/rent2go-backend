package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands;

import java.math.BigDecimal;
import java.util.List;

/**
 * RegisterVehicleWithImageCommand
 * 
 * Command to register a new vehicle with a primary image upload.
 * The image file is uploaded to Cloudinary and assigned as the primary image.
 * 
 * CQRS Pattern: Modifying command that triggers a state change.
 */
public record RegisterVehicleWithImageCommand(
        Long ownerId,
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
        String fuelType,
        List<String> featureNames,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl
) {
}
