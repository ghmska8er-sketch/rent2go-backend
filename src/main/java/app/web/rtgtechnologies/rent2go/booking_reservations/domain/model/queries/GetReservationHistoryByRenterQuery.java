package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries;

/**
 * Query object for retrieving reservation history of a renter (completed bookings).
 */
public record GetReservationHistoryByRenterQuery(Long renterId) {
}
