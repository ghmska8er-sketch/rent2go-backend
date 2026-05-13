package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public record UserResource(
        Long id,
        String email,
        String username,
        String status,
        @JsonProperty("email_verified")
        Boolean emailVerified,
        @JsonProperty("two_factor_enabled")
        Boolean twoFactorEnabled,
        @JsonProperty("created_at")
        Date createdAt,
        @JsonProperty("updated_at")
        Date updatedAt
) {
}
