package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitKycResource(
        @NotNull Long userId,
        @NotBlank String fullName,
        @NotBlank String idNumber,
        String documentUrl
) {}
