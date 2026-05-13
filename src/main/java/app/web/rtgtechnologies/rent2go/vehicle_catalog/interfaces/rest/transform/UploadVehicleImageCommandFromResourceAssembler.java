package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UploadVehicleImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UploadVehicleImageResource;

/**
 * UploadVehicleImageCommandFromResourceAssembler
 *
 * Transforms UploadVehicleImageResource into UploadVehicleImageCommand.
 */
public final class UploadVehicleImageCommandFromResourceAssembler {

    public static UploadVehicleImageCommand toCommand(Long vehicleId, UploadVehicleImageResource resource) {
        return new UploadVehicleImageCommand(
            vehicleId,
            resource.getImagePath(),
            resource.getImageUrl(),
            resource.getIsPrimary()
        );
    }
}
