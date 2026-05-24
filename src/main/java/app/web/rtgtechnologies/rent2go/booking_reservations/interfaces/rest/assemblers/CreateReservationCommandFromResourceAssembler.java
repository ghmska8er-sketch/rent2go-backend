package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.CreateReservationResource;
import org.springframework.stereotype.Component;

@Component
public class CreateReservationCommandFromResourceAssembler {

    public CreateReservationCommand toCommand(CreateReservationResource r) {
        return new CreateReservationCommand(
            r.vehicleId(),
            r.renterId(),
            r.ownerId(),
            r.startDate(),
            r.endDate(),
            r.totalAmount()
            , r.pickupLocation(),
            r.returnLocation(),
            r.coveragePlan(),
            r.pickupPhotos(),
            r.returnPhotos()
        );
    }
}
