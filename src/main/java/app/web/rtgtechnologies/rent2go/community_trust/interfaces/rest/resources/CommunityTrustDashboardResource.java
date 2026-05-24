package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import java.math.BigDecimal;

public record CommunityTrustDashboardResource(
    long totalReviews,
    long approvedReviews,
    long rejectedReviews,
    long flaggedReviews,
    long openReports,
    long resolvedReports,
    long blockedUsers,
    long activeConversations,
    long totalMessages,
    BigDecimal averageTrustScore
) {
}