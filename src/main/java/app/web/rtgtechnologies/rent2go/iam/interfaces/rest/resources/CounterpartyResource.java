package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CounterpartyResource
 *
 * Minimal, read-only projection of a {@code User} used to embed a counterparty's identity
 * (renter/owner in a reservation, the other participant in a conversation) inside another
 * bounded context's resource — e.g. {@code ReservationResource}, {@code ConversationResource}.
 *
 * TS18 — reused across booking_reservations and community_trust to avoid raw numeric-ID renders
 * on mobile clients, following the same community_trust -> iam cross-context read precedent
 * already established in ReviewCommandServiceImpl.
 *
 * Sprint 5 (US76/TS23, BRD-2026-07-05-Reservation-Vehicle-Counterparty-Enrichment.md) —
 * additive extension: {@code dniVerified}/{@code licenseVerified} split the previously
 * collapsed {@code kycVerified} flag into two distinct signals (both derived from the same
 * most-recent {@code KycApplication} record, since that entity does not yet track per-document
 * status separately), and {@code profileImageUrl} surfaces the counterparty's profile photo.
 * {@code kycVerified} is preserved unchanged for backward compatibility with existing
 * consumers, per this BRD's additive-only contract-change requirement (§7.2 R-003). Only
 * derived booleans and the already-public profile photo URL are exposed — never raw
 * {@code KycApplication} document URLs or the ID number (§4.4/§7.2 R-004).
 */
public record CounterpartyResource(
    Long id,
    @JsonProperty("full_name")
    String fullName,
    @JsonProperty("kyc_verified")
    Boolean kycVerified,
    @JsonProperty("dni_verified")
    Boolean dniVerified,
    @JsonProperty("license_verified")
    Boolean licenseVerified,
    @JsonProperty("profile_image_url")
    String profileImageUrl
) {
}
