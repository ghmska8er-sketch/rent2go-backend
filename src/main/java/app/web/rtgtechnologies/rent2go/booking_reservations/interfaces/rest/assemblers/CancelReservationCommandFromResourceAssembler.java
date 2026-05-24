package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CancelReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.CancelReservationResource;
import org.springframework.stereotype.Component;

@Component
public class CancelReservationCommandFromResourceAssembler {

    public CancelReservationCommand toCommand(Long reservationId, CancelReservationResource r) {
        return new CancelReservationCommand(reservationId, r.requestedById(), r.reason());
    }
}
