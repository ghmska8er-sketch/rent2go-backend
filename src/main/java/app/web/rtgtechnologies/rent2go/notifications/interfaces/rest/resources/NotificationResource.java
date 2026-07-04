package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources;

public record NotificationResource(
    Long id,
    Long userId,
    String type,
    String message,
    String readAt,
    String createdAt
) {
}
