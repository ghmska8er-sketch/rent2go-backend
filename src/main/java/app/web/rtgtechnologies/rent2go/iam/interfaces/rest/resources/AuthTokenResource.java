package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root cause fix (Fix 3, KYC display): this record had NO @JsonProperty
 * annotations at all, so Jackson serialized every field with its raw
 * camelCase Java name — including "kycVerified". The Kotlin client's
 * LoginResponse DTO (this endpoint's consumer, POST /auth/login) expects
 * camelCase for every field EXCEPT kycVerified, which it deliberately reads
 * via @SerialName("kyc_verified") to match UserResource's snake_case
 * convention used on GET /auth/me. Because this resource never emitted
 * "kyc_verified", kotlinx.serialization silently fell back to kycVerified's
 * default (false) immediately after login, even when the backend's real
 * value was true — until the user manually refreshed (which calls
 * GET /auth/me via UserResource, which was already correct). Only
 * kycVerified is annotated here to match the client's actual expectation;
 * the other fields are left as-is since the client already expects
 * camelCase for them and changing that would break parsing instead.
 */
public record AuthTokenResource(
        String token,
        Long userId,
        String email,
        String username,
        String fullName,
        String phone,
        String accountType,
        String status,
        Boolean emailVerified,
        Boolean phoneVerified,
        Boolean twoFactorEnabled,
        String profileImageUrl,
        @JsonProperty("kyc_verified")
        Boolean kycVerified
) {
}
