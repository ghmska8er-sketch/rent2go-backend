package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyEmailResource(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "token is required")
        String token
) {
}
