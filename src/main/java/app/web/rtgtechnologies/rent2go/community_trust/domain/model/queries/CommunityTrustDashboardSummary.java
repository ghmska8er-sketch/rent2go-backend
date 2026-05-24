package app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries;

import java.math.BigDecimal;

public record CommunityTrustDashboardSummary(
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