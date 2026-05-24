package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record SendMessageCommand(
    Long conversationId,
    Long senderId,
    String content
) {
}