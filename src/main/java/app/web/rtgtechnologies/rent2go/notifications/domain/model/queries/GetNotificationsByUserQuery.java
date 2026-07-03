package app.web.rtgtechnologies.rent2go.notifications.domain.model.queries;

public record GetNotificationsByUserQuery(
    Long userId,
    int page,
    int size
) {
}
