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
 */
public record CounterpartyResource(
    Long id,
    @JsonProperty("full_name")
    String fullName,
    @JsonProperty("kyc_verified")
    Boolean kycVerified
) {
}
