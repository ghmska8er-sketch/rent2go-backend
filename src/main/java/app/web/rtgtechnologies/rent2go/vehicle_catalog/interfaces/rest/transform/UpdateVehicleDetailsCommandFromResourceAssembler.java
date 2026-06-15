package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UpdateVehicleDetailsCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UpdateVehicleDetailsResource;

import java.math.BigDecimal;

/**
 * UpdateVehicleDetailsCommandFromResourceAssembler
 *
 * Transforms UpdateVehicleDetailsResource into UpdateVehicleDetailsCommand.
 */
public final class UpdateVehicleDetailsCommandFromResourceAssembler {

    public static UpdateVehicleDetailsCommand toCommand(Long vehicleId, UpdateVehicleDetailsResource resource) {
        return new UpdateVehicleDetailsCommand(
            vehicleId,
            resource.getCategoryId(),
            resource.getMake(),
            resource.getModel(),
            resource.getYear(),
            resource.getLocation(),
            resource.getDescription(),
            resource.getSeats(),
            resource.getTransmission(),
            resource.getFuelType(),
            resource.getFeatures(),
            resource.getLatitude() != null ? BigDecimal.valueOf(resource.getLatitude()) : null,
            resource.getLongitude() != null ? BigDecimal.valueOf(resource.getLongitude()) : null
        );
    }
}
