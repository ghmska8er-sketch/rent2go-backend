package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

public record TrustReportResource(
    Long id,
    String subjectType,
    Long subjectId,
    Long reservationId,
    Long reviewId,
    Long reportedUserId,
    Long reporterId,
    String reason,
    String status,
    String moderationNote,
    String createdAt,
    String updatedAt
) {
}