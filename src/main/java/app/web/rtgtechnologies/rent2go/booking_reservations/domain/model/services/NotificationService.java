package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

/**
 * NotificationService
 *
 * Port for sending notifications related to reservations. Implementations may
 * forward to email, push, message broker, or application events.
 */
public interface NotificationService {

    void notifyReservationStatusChanged(Long reservationId, String previousStatus, String newStatus);

    void notifyReservationCreated(Long reservationId, Long renterId, Long ownerId);

    void notifyReservationCancelled(Long reservationId, String reason);
}
