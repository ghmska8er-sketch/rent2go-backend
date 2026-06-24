package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest;

import app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices.DeviceTokenCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.application.internal.queryservices.DeviceTokenQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.UnregisterDeviceTokenCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetDeviceTokensByUserQuery;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers.DeviceTokenResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers.RegisterDeviceTokenCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.DeviceTokenResource;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.RegisterDeviceTokenResource;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
}