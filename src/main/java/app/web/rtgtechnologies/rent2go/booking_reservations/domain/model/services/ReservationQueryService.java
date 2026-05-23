package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationByIdQuery;

import java.util.Optional;
import java.util.List;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerQuery;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByRenterQuery;

/**
 * ReservationQueryService
 *
 * Port for booking queries.
 */
public interface ReservationQueryService {

    Optional<Reservation> handle(GetReservationByIdQuery query);

    List<Reservation> handle(GetReservationsByRenterQuery query);

    List<Reservation> handle(GetReservationsByOwnerQuery query);
}