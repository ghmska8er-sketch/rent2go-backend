package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources;

public record DeviceTokenResource(
    Long id,
    Long userId,
    String platform,
    String deviceName,
    Boolean enabled,
    String createdAt,
    String updatedAt
) {
}