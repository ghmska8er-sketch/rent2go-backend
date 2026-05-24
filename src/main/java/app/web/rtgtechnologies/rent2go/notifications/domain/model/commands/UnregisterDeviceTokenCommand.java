package app.web.rtgtechnologies.rent2go.notifications.domain.model.commands;

public record UnregisterDeviceTokenCommand(Long deviceTokenId, Long userId) {
    public UnregisterDeviceTokenCommand {
        if (deviceTokenId == null) {
            throw new IllegalArgumentException("deviceTokenId is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }
}