package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.AddFavoriteCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.AddFavoriteResource;
import org.springframework.stereotype.Component;

@Component
public class AddFavoriteCommandFromResourceAssembler {

    public AddFavoriteCommand toCommand(AddFavoriteResource r) {
        return new AddFavoriteCommand(r.renterId(), r.vehicleId());
    }
}
