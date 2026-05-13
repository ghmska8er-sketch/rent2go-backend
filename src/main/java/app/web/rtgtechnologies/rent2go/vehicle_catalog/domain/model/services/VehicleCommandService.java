package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RegisterVehicleCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.SetPrimaryVehicleImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RemoveVehicleImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UpdateVehicleDetailsCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UpdateVehiclePricingCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UploadVehicleImageCommand;

/**
 * VehicleCommandService
 *
 * Domain service interface for vehicle command handling.
 * Defines contract for command handlers in the application layer.
 *
 * Hexagonal Architecture: Service port (domain layer interface)
 */
public interface VehicleCommandService {

    /**
     * Register a new vehicle in the catalog
     *
     * @param command RegisterVehicleCommand with vehicle details
     * @return Created Vehicle aggregate
     */
    Vehicle handle(RegisterVehicleCommand command);

    /**
     * Update pricing for a specific vehicle
     *
     * @param command UpdateVehiclePricingCommand with pricing details
     * @return Updated Vehicle aggregate
     */
    Vehicle handle(UpdateVehiclePricingCommand command);

    /**
     * Update editable details of a vehicle
     *
     * @param command UpdateVehicleDetailsCommand with vehicle profile details
     * @return Updated Vehicle aggregate
     */
    Vehicle handle(UpdateVehicleDetailsCommand command);

    /**
     * Upload and attach an image to a vehicle
     *
     * @param command UploadVehicleImageCommand with image details
     * @return Updated Vehicle aggregate with new image
     */
    Vehicle handle(UploadVehicleImageCommand command);

    /**
     * Remove an image from a vehicle
     *
     * @param command RemoveVehicleImageCommand with vehicle and image IDs
     * @return Updated Vehicle aggregate with image removed
     */
    Vehicle handle(RemoveVehicleImageCommand command);

    /**
     * Mark one existing image as the primary image for a vehicle
     *
     * @param command SetPrimaryVehicleImageCommand with vehicle and image IDs
     * @return Updated Vehicle aggregate
     */
    Vehicle handle(SetPrimaryVehicleImageCommand command);
}
