package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries;

/**
 * Query to retrieve reservations for an owner, optionally filtered by status.
 */
public record GetReservationsByOwnerQuery(Long ownerId, String status) {
}
