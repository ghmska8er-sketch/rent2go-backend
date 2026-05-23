package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CancelReservationCommand;

/**
 * ReservationCommandService
 *
 * Port for booking commands.
 */
public interface ReservationCommandService {

    Reservation handle(CreateReservationCommand command);

    Reservation handle(CancelReservationCommand command);
}