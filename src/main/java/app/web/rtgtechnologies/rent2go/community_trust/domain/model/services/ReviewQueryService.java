package app.web.rtgtechnologies.rent2go.community_trust.domain.model.services;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Review;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.UserReputation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetAverageRatingQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.CommunityTrustDashboardSummary;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetCommunityTrustDashboardQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByVehicleQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetUserReputationQuery;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewQueryService {

    List<Review> handle(GetReviewsByVehicleQuery query);

    List<Review> handle(GetReviewsByUserQuery query);

    Optional<UserReputation> handle(GetUserReputationQuery query);

    Optional<BigDecimal> handle(GetAverageRatingQuery query);

    CommunityTrustDashboardSummary handle(GetCommunityTrustDashboardQuery query);
}