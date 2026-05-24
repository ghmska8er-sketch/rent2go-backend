package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.valueobjects.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDeviceTokenResource(
    @NotNull Long userId,
    @NotBlank String token,
    @NotNull DevicePlatform platform,
    String deviceName
) {
}