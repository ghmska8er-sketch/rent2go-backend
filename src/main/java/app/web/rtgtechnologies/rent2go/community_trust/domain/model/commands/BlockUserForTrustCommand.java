package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record BlockUserForTrustCommand(
    Long userId,
    Long moderatorId,
    String reason
) {
}