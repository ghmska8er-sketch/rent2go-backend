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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final DeviceTokenCommandServiceImpl commandService;
    private final DeviceTokenQueryServiceImpl queryService;

    @PostMapping("/device-tokens")
    public ResponseEntity<DeviceTokenResource> registerDeviceToken(@Valid @RequestBody RegisterDeviceTokenResource resource) {
        var command = RegisterDeviceTokenCommandFromResourceAssembler.toCommand(resource);
        var saved = commandService.handle(command).orElseThrow();
        return ResponseEntity.created(URI.create("/api/v1/notifications/device-tokens/" + saved.getId()))
            .body(DeviceTokenResourceFromEntityAssembler.toResource(saved));
    }

    @GetMapping("/users/{userId}/device-tokens")
    public ResponseEntity<List<DeviceTokenResource>> getDeviceTokensByUser(@PathVariable Long userId) {
        var results = queryService.handle(new GetDeviceTokensByUserQuery(userId));
        return ResponseEntity.ok(results.stream().map(DeviceTokenResourceFromEntityAssembler::toResource).toList());
    }

    @DeleteMapping("/users/{userId}/device-tokens/{deviceTokenId}")
    public ResponseEntity<Void> unregisterDeviceToken(@PathVariable Long userId, @PathVariable Long deviceTokenId) {
        var deleted = commandService.handle(new UnregisterDeviceTokenCommand(deviceTokenId, userId));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}