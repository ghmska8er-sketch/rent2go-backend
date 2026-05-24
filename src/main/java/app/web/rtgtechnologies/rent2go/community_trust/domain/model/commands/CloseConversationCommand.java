package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record CloseConversationCommand(
    Long conversationId,
    Long userId
) {
}