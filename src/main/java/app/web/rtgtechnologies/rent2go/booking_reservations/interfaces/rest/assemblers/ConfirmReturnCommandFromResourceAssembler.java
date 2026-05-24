package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.ConfirmReturnCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ConfirmReturnResource;
import org.springframework.stereotype.Component;

@Component
public class ConfirmReturnCommandFromResourceAssembler {

    public ConfirmReturnCommand toCommand(Long reservationId, ConfirmReturnResource resource) {
        if (resource == null) return null;
        return new ConfirmReturnCommand(reservationId, resource.getActorId());
    }
}
