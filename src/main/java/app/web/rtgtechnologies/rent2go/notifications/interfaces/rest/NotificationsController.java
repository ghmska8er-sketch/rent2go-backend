package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest;

import app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices.DeviceTokenCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices.NotificationCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.application.internal.queryservices.DeviceTokenQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.application.internal.queryservices.NotificationQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.UnregisterDeviceTokenCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetDeviceTokensByUserQuery;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetNotificationsByUserQuery;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers.DeviceTokenResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers.NotificationResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers.RegisterDeviceTokenCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.DeviceTokenResource;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.NotificationResource;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.RegisterDeviceTokenResource;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resource.PagedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Notifications", description = "Device token registration and notification delivery support")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final DeviceTokenCommandServiceImpl commandService;
    private final DeviceTokenQueryServiceImpl queryService;
    private final NotificationCommandServiceImpl notificationCommandService;
    private final NotificationQueryServiceImpl notificationQueryService;
    private final CurrentUserService currentUserService;

    @PostMapping("/device-tokens")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Register device token", description = "Stores or refreshes a push notification token for a user's device.")
    public ResponseEntity<DeviceTokenResource> registerDeviceToken(@Valid @RequestBody RegisterDeviceTokenResource resource) {
        var command = RegisterDeviceTokenCommandFromResourceAssembler.toCommand(resource);
        var saved = commandService.handle(command).orElseThrow();
        return ResponseEntity.created(URI.create("/api/v1/notifications/device-tokens/" + saved.getId()))
            .body(DeviceTokenResourceFromEntityAssembler.toResource(saved));
    }

    @GetMapping("/users/{userId}/device-tokens")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List device tokens", description = "Returns all device tokens registered for a user.")
    public ResponseEntity<List<DeviceTokenResource>> getDeviceTokensByUser(@PathVariable Long userId) {
        var results = queryService.handle(new GetDeviceTokensByUserQuery(userId));
        return ResponseEntity.ok(results.stream().map(DeviceTokenResourceFromEntityAssembler::toResource).toList());
    }

    @DeleteMapping("/users/{userId}/device-tokens/{deviceTokenId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Unregister device token", description = "Deletes a previously registered device token for the specified user.")
    public ResponseEntity<Void> unregisterDeviceToken(@PathVariable Long userId, @PathVariable Long deviceTokenId) {
        var deleted = commandService.handle(new UnregisterDeviceTokenCommand(deviceTokenId, userId));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List notifications", description = "Returns a user's in-app notifications, paged and ordered chronologically (most recent first). Page starts at 1 (TS11/US50/US51).")
    public ResponseEntity<PagedResponse<NotificationResource>> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size) {
        if (!currentUserService.isOwnerOrAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var results = notificationQueryService.handle(new GetNotificationsByUserQuery(userId, page - 1, size));
        var content = results.getContent().stream().map(NotificationResourceFromEntityAssembler::toResource).toList();
        return ResponseEntity.ok(new PagedResponse<>(content, page, size, results.getTotalElements(), results.getTotalPages()));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read. Only the notification's own recipient may mark it read (US52).")
    public ResponseEntity<NotificationResource> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        if (!currentUserService.isOwnerOrAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return notificationCommandService.handle(new MarkNotificationAsReadCommand(id, userId))
                .map(notification -> ResponseEntity.ok(NotificationResourceFromEntityAssembler.toResource(notification)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}