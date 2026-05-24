package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserResource(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 150, message = "Full name must be between 3 and 150 characters")
        String fullName,

        @NotBlank(message = "Phone is required")
        @Size(min = 7, max = 20, message = "Phone must be between 7 and 20 characters")
        String phone,

        String profileImageUrl,

        @NotNull(message = "Account type is required")
        AccountType accountType
) {
}
