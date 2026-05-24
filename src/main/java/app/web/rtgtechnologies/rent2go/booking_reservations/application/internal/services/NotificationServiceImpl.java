package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.NotificationService;
import app.web.rtgtechnologies.rent2go.notifications.infrastructure.persistence.jpa.repositories.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final DeviceTokenRepository deviceTokenRepository;

    public NotificationServiceImpl(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public void notifyReservationStatusChanged(Long reservationId, Long renterId, Long ownerId, String previousStatus, String newStatus) {
        log.info("[Notification] Reservation {} status changed from {} to {}", reservationId, previousStatus, newStatus);
        dispatch(renterId, "Reservation status changed", "Reservation %s changed from %s to %s".formatted(reservationId, previousStatus, newStatus));
        dispatch(ownerId, "Reservation status changed", "Reservation %s changed from %s to %s".formatted(reservationId, previousStatus, newStatus));
    }

    @Override
    public void notifyReservationCreated(Long reservationId, Long renterId, Long ownerId) {
        log.info("[Notification] Reservation {} created (renter={}, owner={})", reservationId, renterId, ownerId);
        dispatch(renterId, "Reservation created", "Reservation %s was created".formatted(reservationId));
        dispatch(ownerId, "Reservation created", "Reservation %s was created".formatted(reservationId));
    }

    @Override
    public void notifyReservationCancelled(Long reservationId, Long renterId, Long ownerId, String reason) {
        log.info("[Notification] Reservation {} cancelled: {}", reservationId, reason);
        dispatch(renterId, "Reservation cancelled", "Reservation %s was cancelled: %s".formatted(reservationId, reason));
        dispatch(ownerId, "Reservation cancelled", "Reservation %s was cancelled: %s".formatted(reservationId, reason));
    }

    private void dispatch(Long userId, String title, String message) {
        if (userId == null) {
            return;
        }

        var tokens = deviceTokenRepository.findAllByUserIdAndEnabledTrueOrderByCreatedAtDesc(userId);
        if (tokens.isEmpty()) {
            log.info("[Push] No active device tokens for user {}", userId);
            return;
        }

        for (var token : tokens) {
            log.info("[Push] {} -> user {} token {}: {}", title, userId, token.getToken(), message);
        }
    }
}
