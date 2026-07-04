package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.Notification;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.NotificationResource;

public class NotificationResourceFromEntityAssembler {

    public static NotificationResource toResource(Notification entity) {
        return new NotificationResource(
            entity.getId(),
            entity.getUserId(),
            entity.getType(),
            entity.getMessage(),
            entity.getReadAt() == null ? null : entity.getReadAt().toString(),
            entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString()
        );
    }
}
