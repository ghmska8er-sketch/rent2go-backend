package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Withdrawal;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.WithdrawalStatus;
import app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories.WithdrawalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * WithdrawalService
 *
 * US48/US49/TS12: application service for the owner withdrawal/payout domain. Computes the
 * available balance dynamically (total succeeded earnings minus the sum of all withdrawal
 * amounts already requested for that owner — per the confirmed minimal scope there is no
 * REJECTED/CANCELLED status yet, so every recorded withdrawal counts against the balance)
 * rather than maintaining a separate running-total column.
 */
@Service
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final PaymentsService paymentsService;

    public WithdrawalService(WithdrawalRepository withdrawalRepository, PaymentsService paymentsService) {
        this.withdrawalRepository = withdrawalRepository;
        this.paymentsService = paymentsService;
    }

    /**
     * Computes an owner's total lifetime succeeded earnings (in cents), across all time.
     */
    public long getTotalEarnedCents(Long ownerId) {
        var from = LocalDateTime.of(2000, 1, 1, 0, 0);
        var to = LocalDateTime.now();
        Long total = paymentsService.sumSucceededAmountCentsByOwnerBetween(ownerId, from, to);
        return total != null ? total : 0L;
    }

    /**
     * Computes an owner's currently available balance: total lifetime earnings minus the
     * sum of all withdrawal amounts already requested.
     */
    public long getAvailableBalanceCents(Long ownerId) {
        long totalEarned = getTotalEarnedCents(ownerId);
        long alreadyWithdrawn = getTotalWithdrawnCents(ownerId);
        return Math.max(0L, totalEarned - alreadyWithdrawn);
    }

    public long getTotalWithdrawnCents(Long ownerId) {
        Long sum = withdrawalRepository.sumAmountCentsByOwner(ownerId);
        return sum != null ? sum : 0L;
    }

    public long getPendingWithdrawnCents(Long ownerId) {
        Long sum = withdrawalRepository.sumAmountCentsByOwnerAndStatus(ownerId, WithdrawalStatus.PENDING);
        return sum != null ? sum : 0L;
    }

    /**
     * Requests a withdrawal for the given owner. Throws IllegalArgumentException if the
     * requested amount exceeds the owner's currently available balance.
     *
     * Bugfix (Sprint 5 fixes remaining scope): withdrawals are now persisted and left in
     * PENDING — NOT auto-marked COMPLETED — because there is no real payout rail/bank
     * integration behind this endpoint. Auto-completing a withdrawal that never actually
     * moved money is misleading to the owner (it reads as "you were paid" when nothing was
     * paid) and getAvailableBalanceCents() already counts every non-rejected withdrawal
     * against the available balance regardless of status, so leaving it PENDING does not
     * allow double-withdrawing the same funds. A future admin-approval/payout step can
     * transition PENDING -> COMPLETED (see Withdrawal.complete()) once real settlement exists.
     */
    public Withdrawal requestWithdrawal(Long ownerId, Long amountCents, String payoutDestinationNote) {
        if (amountCents == null || amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        long available = getAvailableBalanceCents(ownerId);
        if (amountCents > available) {
            throw new IllegalArgumentException("Requested amount exceeds available balance");
        }
        var withdrawal = new Withdrawal(ownerId, amountCents, payoutDestinationNote);
        return withdrawalRepository.save(withdrawal);
    }

    public Page<Withdrawal> getWithdrawalHistory(Long ownerId, int page, int size) {
        return withdrawalRepository.findAllByOwnerIdOrderByRequestedAtDesc(ownerId, PageRequest.of(page, size));
    }
}
