package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ConversationResource(
    Long id,
    Long ownerId,
    Long renterId,
    Long vehicleId,
    Long reservationId,
    String subject,
    String status,
    String lastMessageAt,
    String lastMessagePreview,
    String createdAt,
    String updatedAt,
    // TS18 — additive nested counterparty objects; ownerId/renterId are kept unchanged above.
    @JsonProperty("owner")
    CounterpartyResource owner,
    @JsonProperty("renter")
    CounterpartyResource renter
) {
}