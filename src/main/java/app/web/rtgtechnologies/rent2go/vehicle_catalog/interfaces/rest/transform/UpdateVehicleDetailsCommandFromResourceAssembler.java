package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UpdateVehicleDetailsCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UpdateVehicleDetailsResource;

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
            resource.getFuelType()
        );
    }
}
