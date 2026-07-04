package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface VehicleAvailabilityQueryService {

    List<VehicleAvailability> findByVehicleId(Long vehicleId);

    boolean isAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate);

    /**
     * TS20 — bulk read query for availability-aware search.
     *
     * Returns the set of vehicle IDs that are blocked for the given date range, either because of
     * an overlapping {@link VehicleAvailability} block or an overlapping {@link
     * app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation} in
     * one of the blocking statuses: PENDING, CONFIRMED, ACTIVE, RETURN_PENDING, RETURN_CONFIRMED.
     *
     * Intended to be consumed by {@code vehicle_catalog}'s query service to exclude blocked vehicles
     * from search results — kept as a batched set lookup (not a per-vehicle call) to avoid N+1 queries,
     * and kept inside {@code booking_reservations} to respect the DDD bounded-context boundary (no
     * direct JPQL join against {@code vehicle_catalog}'s tables).
     */
    Set<Long> findBlockedVehicleIds(LocalDate startDate, LocalDate endDate);
}
