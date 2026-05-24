package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record RejectReviewCommand(
    Long reviewId,
    Long moderatorId,
    String reason
) {
}