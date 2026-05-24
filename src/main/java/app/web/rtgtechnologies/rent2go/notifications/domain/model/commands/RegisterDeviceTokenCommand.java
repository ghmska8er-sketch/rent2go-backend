package app.web.rtgtechnologies.rent2go.notifications.domain.model.commands;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.valueobjects.DevicePlatform;

public record RegisterDeviceTokenCommand(
    Long userId,
    String token,
    DevicePlatform platform,
    String deviceName
) {
    public RegisterDeviceTokenCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
        if (platform == null) {
            throw new IllegalArgumentException("platform is required");
        }
    }
}