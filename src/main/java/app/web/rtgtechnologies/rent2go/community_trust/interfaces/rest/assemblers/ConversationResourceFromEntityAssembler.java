package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ConversationResource;
import org.springframework.stereotype.Component;

@Component
public class ConversationResourceFromEntityAssembler {

    public ConversationResource toResource(Conversation conversation) {
        return new ConversationResource(
            conversation.getId(),
            conversation.getOwnerId(),
            conversation.getRenterId(),
            conversation.getVehicleId(),
            conversation.getReservationId(),
            conversation.getSubject(),
            conversation.getStatus() == null ? null : conversation.getStatus().name(),
            conversation.getLastMessageAt() == null ? null : conversation.getLastMessageAt().toString(),
            conversation.getLastMessagePreview(),
            conversation.getCreatedAt() == null ? null : conversation.getCreatedAt().toString(),
            conversation.getUpdatedAt() == null ? null : conversation.getUpdatedAt().toString()
        );
    }
}