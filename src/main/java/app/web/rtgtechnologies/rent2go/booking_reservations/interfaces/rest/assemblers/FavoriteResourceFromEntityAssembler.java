package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Favorite;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.FavoriteResource;
import org.springframework.stereotype.Component;

@Component
public class FavoriteResourceFromEntityAssembler {

    public FavoriteResource toResource(Favorite f) {
        return new FavoriteResource(f.getId(), f.getUserId(), f.getVehicleId(), f.getCreatedAt());
    }
}
