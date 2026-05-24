package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType;
import jakarta.validation.constraints.NotNull;

public record RegisterUserResource(
        String email,
        String password,
        String username,
        @NotNull(message = "Account type is required")
        AccountType accountType
) {
}
