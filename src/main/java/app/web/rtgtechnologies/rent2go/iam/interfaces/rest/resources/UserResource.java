package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public record UserResource(
        Long id,
        String email,
        String username,
        @JsonProperty("full_name")
        String fullName,
        String phone,
        @JsonProperty("profile_image_url")
        String profileImageUrl,
        @JsonProperty("account_type")
        String accountType,
        String status,
        @JsonProperty("email_verified")
        Boolean emailVerified,
        @JsonProperty("phone_verified")
        Boolean phoneVerified,
        @JsonProperty("two_factor_enabled")
        Boolean twoFactorEnabled,
        @JsonProperty("kyc_verified")
        Boolean kycVerified,
        @JsonProperty("created_at")
        Date createdAt,
        @JsonProperty("updated_at")
        Date updatedAt
) {
}
