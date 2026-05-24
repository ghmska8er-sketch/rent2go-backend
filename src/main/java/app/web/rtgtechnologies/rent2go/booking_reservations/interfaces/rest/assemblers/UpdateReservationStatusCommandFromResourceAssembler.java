package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UpdateReservationStatusCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.UpdateReservationStatusResource;
import org.springframework.stereotype.Component;

@Component
public class UpdateReservationStatusCommandFromResourceAssembler {

    public UpdateReservationStatusCommand toCommand(Long reservationId, UpdateReservationStatusResource resource) {
        if (resource == null) return null;
        return new UpdateReservationStatusCommand(reservationId, resource.getActorId(), resource.getStatus());
    }
}
