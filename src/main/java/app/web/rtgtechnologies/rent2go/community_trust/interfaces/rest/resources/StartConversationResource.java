package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StartConversationResource(
    @NotNull(message = "Owner id is required") Long ownerId,
    @NotNull(message = "Renter id is required") Long renterId,
    Long vehicleId,
    Long reservationId,
    @Size(max = 120, message = "Subject must be at most 120 characters") String subject
) {
}