package app.web.rtgtechnologies.rent2go.notifications.domain.model.services;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.Notification;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.CreateNotificationCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.MarkNotificationAsReadCommand;

import java.util.Optional;

public interface NotificationCommandService {
    Notification handle(CreateNotificationCommand command);

    Optional<Notification> handle(MarkNotificationAsReadCommand command);
}
