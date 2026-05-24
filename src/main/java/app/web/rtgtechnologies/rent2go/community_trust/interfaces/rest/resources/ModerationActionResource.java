package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerationActionResource(
    @NotNull(message = "Moderator id is required") Long moderatorId,
    @Size(max = 500, message = "Reason must be at most 500 characters") String reason
) {
}