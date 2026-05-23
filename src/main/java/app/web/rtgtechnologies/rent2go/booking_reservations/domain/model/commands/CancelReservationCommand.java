package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands;

/**
 * Command to cancel an existing reservation.
 */
public record CancelReservationCommand(Long reservationId, Long requestedById, String reason) {
}
