package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.DeviceToken;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.DeviceTokenResource;

public class DeviceTokenResourceFromEntityAssembler {

    public static DeviceTokenResource toResource(DeviceToken entity) {
        return new DeviceTokenResource(
            entity.getId(),
            entity.getUserId(),
            entity.getPlatform() == null ? null : entity.getPlatform().name(),
            entity.getDeviceName(),
            entity.getEnabled(),
            entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
            entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }
}