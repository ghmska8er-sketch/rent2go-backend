package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.schedulers;

import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * RES-06: Automatically expires PENDING reservations whose start date has passed.
 * Runs daily at 01:00 server time.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpirationScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expireStaleReservations() {
        var today = LocalDate.now();
        var stale = reservationRepository.findAllPendingExpiredBefore(today);
        if (stale.isEmpty()) return;

        log.info("RES-06: expiring {} stale PENDING reservation(s) as of {}", stale.size(), today);
        for (var reservation : stale) {
            try {
                reservation.expire();
                reservationRepository.save(reservation);
            } catch (IllegalStateException ex) {
                log.warn("RES-06: could not expire reservation {}: {}", reservation.getId(), ex.getMessage());
            }
        }
    }
}
