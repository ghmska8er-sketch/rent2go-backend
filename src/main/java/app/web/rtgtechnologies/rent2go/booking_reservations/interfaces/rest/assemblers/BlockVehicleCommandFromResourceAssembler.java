package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.BlockVehicleCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.BlockRequestResource;
import org.springframework.stereotype.Component;

@Component
public class BlockVehicleCommandFromResourceAssembler {

    public BlockVehicleCommand toCommand(BlockRequestResource r) {
        return new BlockVehicleCommand(r.vehicleId(), r.startDate(), r.endDate(), r.requestedBy());
    }
}
