package app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ReservationRepository
 *
 * Persistence interface for the Reservation aggregate root.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode_Code(String code);

    List<Reservation> findAllByRenterId(Long renterId);
    List<Reservation> findAllByOwnerId(Long ownerId);
    Page<Reservation> findAllByOwnerId(Long ownerId, Pageable pageable);

    List<Reservation> findAllByRenterIdAndStatus_Status(Long renterId, String status);

    List<Reservation> findAllByOwnerIdAndStatus_Status(Long ownerId, String status);
    Page<Reservation> findAllByOwnerIdAndStatus_Status(Long ownerId, String status, Pageable pageable);

    /**
     * Perf fix (2026-07-06), revised per product requirement: GET /api/v1/reservations
     * (renter listing) must return the renter's FULL reservation list — no page/size — because
     * pagination could otherwise split/reorder results such that older reservations appear
     * ahead of more relevant ones. Instead of the prior findAllByRenterId(...) +
     * in-memory List.subList(...) approach (which loaded everything anyway, just to then throw
     * most of it away per page) or a Pageable-based fetch, this issues ONE indexed query
     * (idx_reservations_renter_id) that returns every row already sorted at the database level:
     * non-terminal reservations (PENDING/CONFIRMED/ACTIVE/RETURN_PENDING/RETURN_CONFIRMED, i.e.
     * !BookingStatus.isTerminal()) first, then COMPLETED/CANCELLED/EXPIRED, and within each
     * group by start date descending (most recent/relevant first). Combined with the batched
     * counterparty/vehicle enrichment fix in ReservationResourceFromEntityAssembler, this
     * removes both compounding causes of the 30s-60s response times reported for this endpoint
     * without reintroducing an unbounded "load all, do nothing about the N+1" regression.
     */
    @Query("SELECT r FROM Reservation r WHERE r.renterId = :renterId "
        + "ORDER BY CASE WHEN r.status.status IN ('COMPLETED', 'CANCELLED', 'EXPIRED') THEN 1 ELSE 0 END, "
        + "r.dateRange.startDate DESC")
    List<Reservation> findAllByRenterIdOrderByPriorityThenStartDateDesc(@Param("renterId") Long renterId);

    @Query("SELECT r FROM Reservation r WHERE r.renterId = :renterId AND r.status.status = :status "
        + "ORDER BY CASE WHEN r.status.status IN ('COMPLETED', 'CANCELLED', 'EXPIRED') THEN 1 ELSE 0 END, "
        + "r.dateRange.startDate DESC")
    List<Reservation> findAllByRenterIdAndStatus_StatusOrderByPriorityThenStartDateDesc(@Param("renterId") Long renterId, @Param("status") String status);

    List<Reservation> findAllByVehicleId(Long vehicleId);

    List<Reservation> findAllByVehicleIdAndIdNot(Long vehicleId, Long id);

    @Query("SELECT r FROM Reservation r WHERE r.status.status = 'PENDING' AND r.dateRange.startDate < :today")
    List<Reservation> findAllPendingExpiredBefore(@Param("today") LocalDate today);

    /**
     * TS20 — availability-aware search support.
     * Returns all reservations in a blocking status, for in-memory date-overlap filtering
     * (kept in-memory/batched rather than a per-vehicle query to avoid N+1, consistent with
     * VehicleAvailabilityQueryServiceImpl's existing style).
     */
    @Query("SELECT r FROM Reservation r WHERE r.status.status IN ('PENDING', 'CONFIRMED', 'ACTIVE', 'RETURN_PENDING', 'RETURN_CONFIRMED')")
    List<Reservation> findAllInBlockingStatus();
}