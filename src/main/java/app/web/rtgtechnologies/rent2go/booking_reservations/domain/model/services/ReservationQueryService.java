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

    List<Reservation> handle(app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationHistoryByRenterQuery query);

    org.springframework.data.domain.Page<Reservation> handle(app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerPagedQuery query);

    /**
     * Perf fix (2026-07-06), revised per product requirement: returns the renter's FULL
     * reservation list (no pagination), sorted with non-terminal reservations
     * (PENDING/CONFIRMED/ACTIVE/RETURN_PENDING/RETURN_CONFIRMED) before terminal ones
     * (COMPLETED/CANCELLED/EXPIRED), most recent start date first within each group. Backs
     * GET /api/v1/reservations. See {@code ReservationRepository.findAllByRenterIdOrderByPriorityThenStartDateDesc}
     * for the single-query implementation.
     */
    List<Reservation> handleRenterListPrioritized(GetReservationsByRenterQuery query);
}