package app.web.rtgtechnologies.rent2go.notifications.domain.model.services;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.Notification;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetNotificationsByUserQuery;
import org.springframework.data.domain.Page;

public interface NotificationQueryService {
    Page<Notification> handle(GetNotificationsByUserQuery query);
}
