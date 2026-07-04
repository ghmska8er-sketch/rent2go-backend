package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest;

import app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices.DeviceTokenCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices.NotificationCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.application.internal.queryservices.DeviceTokenQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.application.internal.queryservices.NotificationQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.Notification;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.CreateNotificationCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetNotificationsByUserQuery;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * NotificationsControllerTest
 *
 * Phase 4 (TS11/US50/US51/US52): GET /users/{userId} and PATCH /{id}/read must be
 * ownership-checked (403 for a non-owner caller) and function correctly for the real owner,
 * alongside the pre-existing device-token endpoints (unmodified/untested here — regression
 * for those is covered by not touching their behavior).
 */
@ExtendWith(MockitoExtension.class)
class NotificationsControllerTest {

    @Mock private DeviceTokenCommandServiceImpl commandService;
    @Mock private DeviceTokenQueryServiceImpl queryService;
    @Mock private NotificationCommandServiceImpl notificationCommandService;
    @Mock private NotificationQueryServiceImpl notificationQueryService;
    @Mock private CurrentUserService currentUserService;

    private NotificationsController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationsController(
                commandService,
                queryService,
                notificationCommandService,
                notificationQueryService,
                currentUserService
        );
    }

    @Test
    void getNotificationsByUser_returnsOk_whenCallerIsRealUser() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(true);
        Notification notification = new Notification(new CreateNotificationCommand(1L, "RESERVATION", "hello"));
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationQueryService.handle(new GetNotificationsByUserQuery(1L, 0, 20))).thenReturn(page);

        var response = controller.getNotificationsByUser(1L, 1, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().content().size());
    }

    @Test
    void getNotificationsByUser_returnsForbidden_whenCallerIsDifferentUser() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(false);

        var response = controller.getNotificationsByUser(1L, 1, 20);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void markAsRead_returnsOk_whenNotificationBelongsToCaller() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(true);
        Notification notification = new Notification(new CreateNotificationCommand(1L, "RESERVATION", "hello"));
        notification.markAsRead();
        when(notificationCommandService.handle(new MarkNotificationAsReadCommand(100L, 1L))).thenReturn(Optional.of(notification));

        var response = controller.markAsRead(100L, 1L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void markAsRead_returnsForbidden_whenCallerIsNotTheOwner() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(false);

        var response = controller.markAsRead(100L, 1L);

        assertEquals(403, response.getStatusCode().value());
    }
}
