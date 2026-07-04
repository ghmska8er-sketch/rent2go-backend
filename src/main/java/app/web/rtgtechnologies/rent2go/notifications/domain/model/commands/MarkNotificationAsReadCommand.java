package app.web.rtgtechnologies.rent2go.notifications.domain.model.commands;

public record MarkNotificationAsReadCommand(
    Long notificationId,
    Long userId
) {
}
