package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.VehiclePerformanceResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * VehiclePerformanceServiceImpl
 *
 * US24: Computes per-vehicle performance metrics by aggregating data from
 * the booking_reservations context (reservation count, occupancy) and the
 * payments context (revenue). Depends only on repository interfaces per
 * the layered architecture (controller -> service -> repository -> domain).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiclePerformanceServiceImpl implements VehiclePerformanceService {

    private static final Logger log = LoggerFactory.getLogger(VehiclePerformanceServiceImpl.class);

    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentsService paymentsService;

    @Override
    public VehiclePerformanceResource getPerformance(Long vehicleId, LocalDate from, LocalDate to) {
        log.info("Computing vehicle performance for vehicleId={}, from={}, to={}", vehicleId, from, to);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> {
                    log.error("Vehicle performance requested for nonexistent vehicleId={}", vehicleId);
                    return new IllegalArgumentException("Vehicle not found: " + vehicleId);
                });

        LocalDate periodStart = from != null ? from : vehiclePublishedDate(vehicle);
        LocalDate periodEnd = to != null ? to : LocalDate.now();

        if (periodStart.isAfter(periodEnd)) {
            log.error("Invalid date range for vehicle performance: from={} is after to={}", periodStart, periodEnd);
            throw new IllegalArgumentException("From date must not be after to date");
        }

        LocalDateTime fromDateTime = periodStart.atStartOfDay();
        LocalDateTime toDateTime = periodEnd.atTime(23, 59, 59);

        List<Reservation> reservations = reservationRepository.findAllByVehicleId(vehicleId);

        // Reservations that overlap the requested period, counted for reservationCount.
        List<Reservation> reservationsInPeriod = reservations.stream()
                .filter(r -> r.getDateRange() != null)
                .filter(r -> !r.getDateRange().getStartDate().isAfter(periodEnd)
                        && !r.getDateRange().getEndDate().isBefore(periodStart))
                .toList();

        int reservationCount = reservationsInPeriod.size();

        Long totalCents = paymentsService.sumSucceededAmountCentsByVehicleBetween(vehicleId, fromDateTime, toDateTime);
        long cents = totalCents != null ? totalCents : 0L;
        BigDecimal totalRevenue = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        double occupancyPercentage = calculateOccupancyPercentage(reservationsInPeriod, periodStart, periodEnd, vehiclePublishedDate(vehicle));

        VehiclePerformanceResource resource = new VehiclePerformanceResource();
        resource.setVehicleId(vehicleId);
        resource.setFrom(periodStart.toString());
        resource.setTo(periodEnd.toString());
        resource.setReservationCount(reservationCount);
        resource.setTotalRevenue(totalRevenue);
        // US69 (display-label-only, per BRD-2026-07-05-Estrategia-Moneda-Soles-vs-Dolares.md):
        // vehicle performance revenue is user-facing Soles copy. Stripe's actual processing
        // currency is a separate, unaffected code path (PaymentsController.createIntent).
        resource.setCurrency("PEN");
        resource.setOccupancyPercentage(occupancyPercentage);

        log.info("Vehicle performance computed for vehicleId={}: reservationCount={}, totalRevenue={}, occupancyPercentage={}",
                vehicleId, reservationCount, totalRevenue, occupancyPercentage);

        return resource;
    }

    /**
     * ============================================================================
     * ASSUMPTION — PENDING PRODUCT/BUSINESS SIGN-OFF
     * ============================================================================
     * No occupancy definition exists anywhere else in the codebase (verified via
     * a case-insensitive search for "occupancy", "ocupacion", "ocupación" across
     * the entire backend, Flutter, and Kotlin sources — no hits outside the files
     * introduced by this change).
     *
     * Formula used here:
     *   occupancyPercentage = (bookedDaysInPeriod / daysSinceVehiclePublishedOrAvailable) * 100
     *   clamped to the range [0, 100].
     *
     * Where:
     *   - bookedDaysInPeriod = number of days, within [periodStart, periodEnd],
     *     covered by reservations that are NOT cancelled/expired (i.e. any
     *     reservation whose status is PENDING, CONFIRMED, ACTIVE, or COMPLETED),
     *     with overlapping reservation date ranges merged so days are not double
     *     counted.
     *   - daysSinceVehiclePublishedOrAvailable = number of days between the later
     *     of (vehicle creation date, periodStart) and periodEnd, inclusive,
     *     minimum of 1 day to avoid division by zero.
     *
     * This is a reasonable, defensible interpretation, but it has NOT been
     * confirmed by product/business. Do not treat this as authoritative until
     * signed off.
     * ============================================================================
     */
    private double calculateOccupancyPercentage(List<Reservation> reservationsInPeriod, LocalDate periodStart,
                                                 LocalDate periodEnd, LocalDate vehiclePublishedDate) {
        LocalDate windowStart = vehiclePublishedDate.isAfter(periodStart) ? vehiclePublishedDate : periodStart;
        if (windowStart.isAfter(periodEnd)) {
            return 0.0;
        }

        long totalDays = Math.max(1, ChronoUnit.DAYS.between(windowStart, periodEnd) + 1);

        List<Reservation> countable = reservationsInPeriod.stream()
                .filter(r -> !isCancelledOrExpired(r))
                .toList();

        if (countable.isEmpty()) {
            return 0.0;
        }

        // Merge overlapping/adjacent booked ranges (clamped to the window) so days aren't double-counted.
        List<LocalDate[]> ranges = countable.stream()
                .map(r -> {
                    LocalDate s = r.getDateRange().getStartDate().isBefore(windowStart) ? windowStart : r.getDateRange().getStartDate();
                    LocalDate e = r.getDateRange().getEndDate().isAfter(periodEnd) ? periodEnd : r.getDateRange().getEndDate();
                    return new LocalDate[]{s, e};
                })
                .filter(range -> !range[0].isAfter(range[1]))
                .sorted((a, b) -> a[0].compareTo(b[0]))
                .toList();

        long bookedDays = 0;
        LocalDate mergedStart = null;
        LocalDate mergedEnd = null;
        for (LocalDate[] range : ranges) {
            if (mergedStart == null) {
                mergedStart = range[0];
                mergedEnd = range[1];
                continue;
            }
            if (!range[0].isAfter(mergedEnd.plusDays(1))) {
                if (range[1].isAfter(mergedEnd)) {
                    mergedEnd = range[1];
                }
            } else {
                bookedDays += ChronoUnit.DAYS.between(mergedStart, mergedEnd) + 1;
                mergedStart = range[0];
                mergedEnd = range[1];
            }
        }
        if (mergedStart != null) {
            bookedDays += ChronoUnit.DAYS.between(mergedStart, mergedEnd) + 1;
        }

        double occupancy = (bookedDays / (double) totalDays) * 100.0;
        return Math.max(0.0, Math.min(100.0, occupancy));
    }

    private boolean isCancelledOrExpired(Reservation r) {
        String status = r.getStatus() != null ? r.getStatus().getStatus() : null;
        return "CANCELLED".equals(status) || "EXPIRED".equals(status);
    }

    private LocalDate vehiclePublishedDate(Vehicle vehicle) {
        if (vehicle.getCreatedAt() != null) {
            return vehicle.getCreatedAt().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }
        return LocalDate.now();
    }
}
