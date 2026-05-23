package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.ModifyReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ModifyReservationResource;
import org.springframework.stereotype.Component;

@Component
public class ModifyReservationCommandFromResourceAssembler {

    public ModifyReservationCommand toCommand(Long reservationId, ModifyReservationResource r) {
        return new ModifyReservationCommand(reservationId, r.startDate(), r.endDate(), r.requestedBy());
    }
}
