package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.BlockVehicleCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UnblockVehicleCommand;

public interface VehicleAvailabilityCommandService {

    VehicleAvailability handle(BlockVehicleCommand command);

    void handle(UnblockVehicleCommand command);
}
