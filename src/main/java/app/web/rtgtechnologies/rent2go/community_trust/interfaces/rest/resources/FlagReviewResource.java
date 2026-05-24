package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FlagReviewResource(
    @NotNull(message = "Reporter id is required") Long reporterId,
    @NotNull(message = "Reason is required") @Size(max = 500, message = "Reason must be at most 500 characters") String reason
) {
}