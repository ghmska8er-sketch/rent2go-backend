package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UpdateVehiclePricingCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UpdateVehiclePricingResource;

/**
 * UpdateVehiclePricingCommandFromResourceAssembler
 *
 * Transforms UpdateVehiclePricingResource (HTTP DTO) into UpdateVehiclePricingCommand (Domain Command).
 * Encapsulates the conversion logic between interfaces and domain layers.
 *
 * Hexagonal Architecture: Assembler (interfaces layer)
 */
public final class UpdateVehiclePricingCommandFromResourceAssembler {

    /**
     * Convert HTTP resource to domain command
     *
     * @param resource UpdateVehiclePricingResource from HTTP layer
     * @return UpdateVehiclePricingCommand for domain layer
     */
    public static UpdateVehiclePricingCommand toCommand(UpdateVehiclePricingResource resource) {
        return new UpdateVehiclePricingCommand(
            resource.getVehicleId(),
            resource.getNewDailyPrice()
        );
    }
}
