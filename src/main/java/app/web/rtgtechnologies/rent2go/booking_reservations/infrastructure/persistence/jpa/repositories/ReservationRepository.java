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