package app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Withdrawal;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    Page<Withdrawal> findAllByOwnerIdOrderByRequestedAtDesc(Long ownerId, Pageable pageable);

    /**
     * Sum of all withdrawal amounts (in cents) ever requested by an owner, regardless of
     * status. Per the confirmed minimal scope, no REJECTED/CANCELLED status exists yet, so
     * every recorded withdrawal counts against the available balance.
     */
    @Query("SELECT COALESCE(SUM(w.amountCents), 0) FROM Withdrawal w WHERE w.ownerId = :ownerId")
    Long sumAmountCentsByOwner(@Param("ownerId") Long ownerId);

    /**
     * Sum of withdrawal amounts (in cents) requested by an owner that are still in the given
     * status (used to compute pendingPayoutCents on the earnings report).
     */
    @Query("SELECT COALESCE(SUM(w.amountCents), 0) FROM Withdrawal w WHERE w.ownerId = :ownerId AND w.status = :status")
    Long sumAmountCentsByOwnerAndStatus(@Param("ownerId") Long ownerId, @Param("status") WithdrawalStatus status);
}
