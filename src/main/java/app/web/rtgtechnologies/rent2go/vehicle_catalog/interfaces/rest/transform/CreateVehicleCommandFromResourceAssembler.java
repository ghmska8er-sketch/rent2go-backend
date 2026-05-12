package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RegisterVehicleCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.CreateVehicleResource;

/**
 * CreateVehicleCommandFromResourceAssembler
 *
 * Transforms CreateVehicleResource (HTTP DTO) into RegisterVehicleCommand (Domain Command).
 * Encapsulates the conversion logic between interfaces and domain layers.
 *
 * Hexagonal Architecture: Assembler (interfaces layer)
 */
public final class CreateVehicleCommandFromResourceAssembler {

    /**
     * Convert HTTP resource to domain command
     *
     * @param resource CreateVehicleResource from HTTP layer
     * @return RegisterVehicleCommand for domain layer
     */
    public static RegisterVehicleCommand toCommand(CreateVehicleResource resource) {
        return new RegisterVehicleCommand(
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
            resource.getFuelType()
        );
    }
}
