package app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.Notification;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.CreateNotificationCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.services.NotificationCommandService;
import app.web.rtgtechnologies.rent2go.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;

    public NotificationCommandServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification handle(CreateNotificationCommand command) {
        return notificationRepository.save(new Notification(command));
    }

    @Override
    public Optional<Notification> handle(MarkNotificationAsReadCommand command) {
        var notificationOpt = notificationRepository.findByIdAndUserId(command.notificationId(), command.userId());
        if (notificationOpt.isEmpty()) {
            return Optional.empty();
        }
        var notification = notificationOpt.get();
        notification.markAsRead();
        return Optional.of(notificationRepository.save(notification));
    }
}
