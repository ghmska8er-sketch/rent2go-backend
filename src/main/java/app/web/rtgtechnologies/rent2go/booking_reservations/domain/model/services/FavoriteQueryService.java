package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Favorite;

import java.util.List;

public interface FavoriteQueryService {

    List<Favorite> findByRenterId(Long renterId);
}
