package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SendMessageCommand;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.SendMessageResource;

public class SendMessageCommandFromResourceAssembler {

    public SendMessageCommand toCommand(Long conversationId, SendMessageResource resource) {
        return new SendMessageCommand(conversationId, resource.senderId(), resource.content());
    }
}