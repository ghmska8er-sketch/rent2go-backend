package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void notifyReservationStatusChanged(Long reservationId, String previousStatus, String newStatus) {
        log.info("[Notification] Reservation {} status changed from {} to {}", reservationId, previousStatus, newStatus);
    }

    @Override
    public void notifyReservationCreated(Long reservationId, Long renterId, Long ownerId) {
        log.info("[Notification] Reservation {} created (renter={}, owner={})", reservationId, renterId, ownerId);
    }

    @Override
    public void notifyReservationCancelled(Long reservationId, String reason) {
        log.info("[Notification] Reservation {} cancelled: {}", reservationId, reason);
    }
}
