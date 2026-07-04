package app.web.rtgtechnologies.rent2go.notifications.domain.model.commands;

public record CreateNotificationCommand(
    Long userId,
    String type,
    String message
) {
}
