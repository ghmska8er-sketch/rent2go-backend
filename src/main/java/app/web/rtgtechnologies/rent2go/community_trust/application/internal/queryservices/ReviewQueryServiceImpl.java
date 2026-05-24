package app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Review;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.UserReputation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.CommunityTrustDashboardSummary;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetAverageRatingQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetCommunityTrustDashboardQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByVehicleQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetUserReputationQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.services.ReviewQueryService;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.ConversationRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.ReviewRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.TrustReportRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.MessageRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.UserReputationRepository;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.TrustReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final UserReputationRepository userReputationRepository;
    private final TrustReportRepository trustReportRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public List<Review> handle(GetReviewsByVehicleQuery query) {
        return reviewRepository.findAllByVehicleIdOrderByCreatedAtDesc(query.vehicleId());
    }

    @Override
    public List<Review> handle(GetReviewsByUserQuery query) {
        return reviewRepository.findAllByReviewedUserIdOrderByCreatedAtDesc(query.userId());
    }

    @Override
    public Optional<UserReputation> handle(GetUserReputationQuery query) {
        return userReputationRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<BigDecimal> handle(GetAverageRatingQuery query) {
        var reviews = reviewRepository.findAllByVehicleId(query.vehicleId()).stream()
            .filter(Review::isApproved)
            .toList();

        if (reviews.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal sum = reviews.stream()
            .map(review -> BigDecimal.valueOf(review.getRatingValue()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Optional.of(sum.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP));
    }

    @Override
    public CommunityTrustDashboardSummary handle(GetCommunityTrustDashboardQuery query) {
        long totalReviews = reviewRepository.count();
        long approvedReviews = reviewRepository.findAll().stream().filter(Review::isApproved).count();
        long rejectedReviews = reviewRepository.findAll().stream().filter(Review::isRejected).count();
        long flaggedReviews = reviewRepository.findAll().stream().filter(Review::isFlagged).count();

        var reports = trustReportRepository.findAll();
        long openReports = reports.stream().filter(report -> report.getStatus() == TrustReportStatus.OPEN).count();
        long resolvedReports = reports.stream().filter(report -> report.getStatus() == TrustReportStatus.RESOLVED).count();
        long blockedUsers = userReputationRepository.findAll().stream().filter(UserReputation::isBlocked).count();
        long activeConversations = conversationRepository.findAll().stream()
            .filter(conversation -> conversation.getStatus() != null && "OPEN".equals(conversation.getStatus().name()))
            .count();
        long totalMessages = messageRepository.count();

        BigDecimal averageTrustScore = userReputationRepository.findAll().stream()
            .map(UserReputation::getTrustScore)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long reputationCount = userReputationRepository.count();
        if (reputationCount > 0) {
            averageTrustScore = averageTrustScore.divide(BigDecimal.valueOf(reputationCount), 2, RoundingMode.HALF_UP);
        } else {
            averageTrustScore = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new CommunityTrustDashboardSummary(
            totalReviews,
            approvedReviews,
            rejectedReviews,
            flaggedReviews,
            openReports,
            resolvedReports,
            blockedUsers,
            activeConversations,
            totalMessages,
            averageTrustScore
        );
    }
}