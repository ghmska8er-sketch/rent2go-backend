package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewCategory;

public record ReviewResource(
    Long id,
    Long reservationId,
    Long vehicleId,
    Long reviewerId,
    Long reviewedUserId,
    ReviewCategory category,
    Integer rating,
    String status,
    String comment,
    Integer flagCount,
    String moderationNote,
    String createdAt,
    String updatedAt
) {
}