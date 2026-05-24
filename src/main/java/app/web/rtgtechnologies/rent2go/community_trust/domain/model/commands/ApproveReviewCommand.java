package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record ApproveReviewCommand(
    Long reviewId,
    Long moderatorId,
    String reason
) {
}