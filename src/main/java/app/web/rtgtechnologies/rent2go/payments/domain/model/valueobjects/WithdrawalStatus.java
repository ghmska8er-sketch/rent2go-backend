package app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects;

/**
 * WithdrawalStatus
 *
 * Status of an owner's withdrawal/payout request (US48/US49, extended by Sprint 5's US66).
 * This remains a mock payout system with no real payout rail and no admin-approval workflow
 * — a withdrawal now transitions synchronously from PENDING to COMPLETED at creation time
 * (see {@code WithdrawalService#requestWithdrawal}), rather than staying PENDING forever.
 * Kept as a plain enum (mirroring TrustReportStatus/DevicePlatform) rather than an
 * embeddable value object since no additional per-status behavior is required today.
 */
public enum WithdrawalStatus {
    PENDING,
    COMPLETED
}
