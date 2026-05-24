package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewCategory;

public record SubmitReviewCommand(
    Long reservationId,
    Long vehicleId,
    Long reviewerId,
    Long reviewedUserId,
    ReviewCategory category,
    Integer rating,
    String comment
) {
}