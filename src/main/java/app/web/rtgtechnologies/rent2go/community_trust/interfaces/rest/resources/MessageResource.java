package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

public record MessageResource(
    Long id,
    Long conversationId,
    Long senderId,
    String content,
    String readAt,
    String createdAt,
    String updatedAt,
    Boolean isOnline,
    String lastSeenAt,
    Integer unreadCount
) {
}