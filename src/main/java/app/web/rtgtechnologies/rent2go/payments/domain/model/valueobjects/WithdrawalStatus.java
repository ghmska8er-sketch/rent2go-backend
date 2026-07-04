package app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects;

/**
 * WithdrawalStatus
 *
 * Status of an owner's withdrawal/payout request (US48/US49). Minimal scope for this
 * sprint: requests are created as PENDING and there is no admin-approval workflow yet — a
 * future sprint can extend this enum (e.g. APPROVED/REJECTED/PAID) once that workflow is
 * built. Kept as a plain enum (mirroring TrustReportStatus/DevicePlatform) rather than an
 * embeddable value object since no additional per-status behavior is required today.
 */
public enum WithdrawalStatus {
    PENDING
}
