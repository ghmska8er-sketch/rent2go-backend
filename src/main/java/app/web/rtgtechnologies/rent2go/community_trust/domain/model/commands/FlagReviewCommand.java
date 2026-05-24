package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record FlagReviewCommand(
    Long reviewId,
    Long reporterId,
    String reason
) {
}