package app.web.rtgtechnologies.rent2go.payments.domain.model.entities;

import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Withdrawal
 *
 * US48/US49/TS12: an owner's request to withdraw part of their available earnings.
 * Minimal scope for this sprint (confirmed, not re-litigated):
 * - a single withdrawal method per owner, no multi-method support;
 * - no structured bank-account storage — {@link #payoutDestinationNote} is a free-text,
 *   non-validated string, never PCI/bank data;
 * - no admin-approval workflow yet — every request is created in PENDING and stays there;
 *   {@link #status} exists so a future admin action can update it without a schema change.
 */
@Entity
@Table(name = "withdrawals")
@Getter
@NoArgsConstructor
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "payout_destination_note", length = 500)
    private String payoutDestinationNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WithdrawalStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    public Withdrawal(Long ownerId, Long amountCents, String payoutDestinationNote) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
        if (amountCents == null || amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be positive");
        }
        this.ownerId = ownerId;
        this.amountCents = amountCents;
        this.payoutDestinationNote = payoutDestinationNote;
        this.status = WithdrawalStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * Transitions this withdrawal to COMPLETED. Per US66 (Sprint 5), this remains a mock
     * payout system with no real payout rail — completion happens synchronously right after
     * balance validation and persistence, in the same request, with no async workflow and no
     * admin step.
     */
    public void complete() {
        this.status = WithdrawalStatus.COMPLETED;
    }
}
