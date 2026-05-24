package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Message;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.MessageResource;
import org.springframework.stereotype.Component;

@Component
public class MessageResourceFromEntityAssembler {

    public MessageResource toResource(Message message) {
        return new MessageResource(
            message.getId(),
            message.getConversationId(),
            message.getSenderId(),
            message.getContent(),
            message.getReadAt() == null ? null : message.getReadAt().toString(),
            message.getCreatedAt() == null ? null : message.getCreatedAt().toString(),
            message.getUpdatedAt() == null ? null : message.getUpdatedAt().toString()
            , null, null, null
        );
    }
}