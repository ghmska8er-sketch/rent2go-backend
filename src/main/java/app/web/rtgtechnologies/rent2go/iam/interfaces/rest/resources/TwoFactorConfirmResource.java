package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorConfirmResource(
        @NotBlank String token
) {}
