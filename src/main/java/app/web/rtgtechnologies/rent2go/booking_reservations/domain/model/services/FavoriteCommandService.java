package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Favorite;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.AddFavoriteCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.RemoveFavoriteCommand;

public interface FavoriteCommandService {

    Favorite handle(AddFavoriteCommand command);

    void handle(RemoveFavoriteCommand command);
}
