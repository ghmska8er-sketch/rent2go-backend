package app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.Notification;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.CreateNotificationCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import app.web.rtgtechnologies.rent2go.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * NotificationCommandServiceImplTest
 *
 * Unit tests for Phase 4's notification create/mark-as-read commands (TS11/US50-52). All IO
 * (NotificationRepository) is mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationCommandServiceImpl(notificationRepository);
    }

    @Test
    void handleCreate_savesAndReturnsNewNotification() {
        var command = new CreateNotificationCommand(1L, "RESERVATION", "Your reservation was confirmed");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.handle(command);

        assertEquals(1L, saved.getUserId());
        assertEquals("RESERVATION", saved.getType());
        assertTrue(saved.isUnread());
    }

    @Test
    void handleMarkAsRead_marksAndSaves_whenNotificationBelongsToUser() {
        var notification = new Notification(new CreateNotificationCommand(5L, "RESERVATION", "hello"));
        when(notificationRepository.findByIdAndUserId(100L, 5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        var result = service.handle(new MarkNotificationAsReadCommand(100L, 5L));

        assertTrue(result.isPresent());
        assertTrue(!result.get().isUnread());
    }

    @Test
    void handleMarkAsRead_returnsEmpty_whenNotificationDoesNotBelongToUser() {
        when(notificationRepository.findByIdAndUserId(100L, 999L)).thenReturn(Optional.empty());

        var result = service.handle(new MarkNotificationAsReadCommand(100L, 999L));

        assertEquals(Optional.empty(), result);
    }
}
