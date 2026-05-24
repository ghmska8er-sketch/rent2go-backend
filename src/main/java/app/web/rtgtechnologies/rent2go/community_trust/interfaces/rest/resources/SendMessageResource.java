package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageResource(
    @NotNull(message = "Sender id is required") Long senderId,
    @NotNull(message = "Content is required") @Size(max = 1000, message = "Content must be at most 1000 characters") String content
) {
}