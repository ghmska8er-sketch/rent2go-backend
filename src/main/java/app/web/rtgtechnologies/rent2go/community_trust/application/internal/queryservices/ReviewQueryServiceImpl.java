package app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Review;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.UserReputation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetAverageRatingQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByVehicleQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetUserReputationQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.services.ReviewQueryService;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.ReviewRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.UserReputationRepository;
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
}