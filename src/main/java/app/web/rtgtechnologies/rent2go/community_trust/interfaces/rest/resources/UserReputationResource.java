package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import java.math.BigDecimal;

public record UserReputationResource(
    Long userId,
    Integer approvedReviewCount,
    BigDecimal averageRating,
    Integer trustScore,
    Boolean blocked,
    String lastModerationReason
) {
}