package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries;

/**
 * Query to retrieve reservations for a renter, optionally filtered by status.
 */
public record GetReservationsByRenterQuery(Long renterId, String status) {
}
