package app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    Optional<Payment> findByReservationId(Long reservationId);

    @Query(value = "SELECT SUM(p.amount_cents) FROM payments p JOIN reservations r ON p.reservation_id = r.id " +
            "WHERE r.owner_id = :ownerId AND p.status = 'SUCCEEDED' " +
            "AND p.created_at BETWEEN :from AND :to", nativeQuery = true)
    Long sumSucceededAmountCentsByOwnerBetween(@Param("ownerId") Long ownerId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

        @Query(value = "SELECT COUNT(1) FROM payments p JOIN reservations r ON p.reservation_id = r.id " +
            "WHERE r.owner_id = :ownerId AND p.status = 'SUCCEEDED' " +
            "AND p.created_at BETWEEN :from AND :to", nativeQuery = true)
        Long countSucceededPaymentsByOwnerBetween(@Param("ownerId") Long ownerId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * US24: Sum succeeded payment amounts (in cents) for a single vehicle within a date range.
     * Joins through reservations to filter by vehicle_id.
     */
    @Query(value = "SELECT SUM(p.amount_cents) FROM payments p JOIN reservations r ON p.reservation_id = r.id " +
            "WHERE r.vehicle_id = :vehicleId AND p.status = 'SUCCEEDED' " +
            "AND p.created_at BETWEEN :from AND :to", nativeQuery = true)
    Long sumSucceededAmountCentsByVehicleBetween(@Param("vehicleId") Long vehicleId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * US47/TS12: itemized earnings movements — every payment record (regardless of status)
     * for an owner's vehicles within a date range, most recent first. Reuses the same
     * reservations join as the aggregate sum queries above.
     */
    @Query(value = "SELECT p.* FROM payments p JOIN reservations r ON p.reservation_id = r.id " +
            "WHERE r.owner_id = :ownerId AND p.created_at BETWEEN :from AND :to " +
            "ORDER BY p.created_at DESC", nativeQuery = true)
    List<Payment> findAllByOwnerBetween(@Param("ownerId") Long ownerId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
