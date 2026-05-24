package app.web.rtgtechnologies.rent2go.notifications.domain.model.services;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.DeviceToken;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.RegisterDeviceTokenCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.UnregisterDeviceTokenCommand;

import java.util.Optional;

public interface DeviceTokenCommandService {
    Optional<DeviceToken> handle(RegisterDeviceTokenCommand command);
    boolean handle(UnregisterDeviceTokenCommand command);
}