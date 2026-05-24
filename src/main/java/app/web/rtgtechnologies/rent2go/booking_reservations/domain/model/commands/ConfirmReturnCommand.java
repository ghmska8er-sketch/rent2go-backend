package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands;

/**
 * Command to confirm the return of a vehicle for a reservation (typically by owner).
 */
public record ConfirmReturnCommand(Long reservationId, Long actorId) {
}
