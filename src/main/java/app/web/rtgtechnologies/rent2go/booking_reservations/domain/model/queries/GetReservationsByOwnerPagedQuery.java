package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries;

/**
 * Query for paged owner reservations.
 */
public record GetReservationsByOwnerPagedQuery(Long ownerId, String status, int page, int size) {
}
