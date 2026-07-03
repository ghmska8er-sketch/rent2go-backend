package app.web.rtgtechnologies.rent2go.notifications.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.Notification;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetNotificationsByUserQuery;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.services.NotificationQueryService;
import app.web.rtgtechnologies.rent2go.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationQueryServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Page<Notification> handle(GetNotificationsByUserQuery query) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(query.userId(), PageRequest.of(query.page(), query.size()));
    }
}
