package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.StartConversationCommand;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.StartConversationResource;
import org.springframework.stereotype.Component;

@Component
public class StartConversationCommandFromResourceAssembler {

    public StartConversationCommand toCommand(StartConversationResource resource) {
        return new StartConversationCommand(
            resource.ownerId(),
            resource.renterId(),
            resource.vehicleId(),
            resource.reservationId(),
            resource.subject()
        );
    }
}