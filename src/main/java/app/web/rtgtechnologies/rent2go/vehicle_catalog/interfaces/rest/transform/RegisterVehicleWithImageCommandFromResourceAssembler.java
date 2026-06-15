package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RegisterVehicleWithImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.RegisterVehicleWithImageResource;

import java.math.BigDecimal;
import java.util.List;

/**
 * RegisterVehicleWithImageCommandFromResourceAssembler
 *
 * Transforms RegisterVehicleWithImageResource (HTTP DTO) into RegisterVehicleWithImageCommand (Domain Command).
 *
 * Hexagonal Architecture: Assembler (interfaces layer)
 */
public final class RegisterVehicleWithImageCommandFromResourceAssembler {

    /**
     * Convert HTTP resource to domain command
     *
     * @param ownerId Owner ID from authentication context
     * @param resource RegisterVehicleWithImageResource from HTTP layer
     * @return RegisterVehicleWithImageCommand for domain layer
     */
    public static RegisterVehicleWithImageCommand toCommand(Long ownerId, RegisterVehicleWithImageResource resource, String imageUrl) {
        return new RegisterVehicleWithImageCommand(
            ownerId,
            resource.getLicensePlate(),
            resource.getMake(),
            resource.getModel(),
            resource.getYear(),
            resource.getVin(),
            resource.getDailyPrice(),
            resource.getCategoryId(),
            resource.getLocation(),
            resource.getDescription(),
            resource.getSeats(),
            resource.getTransmission(),
            resource.getFuelType(),
            resource.getFeatureNames(),
            resource.getLatitude() != null ? BigDecimal.valueOf(resource.getLatitude()) : null,
            resource.getLongitude() != null ? BigDecimal.valueOf(resource.getLongitude()) : null,
            imageUrl
        );
    }
}
