package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands;

/**
 * UpdateReservationStatusCommand
 *
 * Command to request a reservation state change by an actor (typically the owner).
 */
public record UpdateReservationStatusCommand(
    Long reservationId,
    Long actorId,
    String status
) {
}
