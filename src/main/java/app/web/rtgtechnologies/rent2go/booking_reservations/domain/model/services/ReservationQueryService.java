package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationByIdQuery;

import java.util.Optional;

/**
 * ReservationQueryService
 *
 * Port for booking queries.
 */
public interface ReservationQueryService {

    Optional<Reservation> handle(GetReservationByIdQuery query);
}