package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.BlockUserForTrustCommand;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.BlockUserResource;
import org.springframework.stereotype.Component;

@Component
public class BlockUserForTrustCommandFromResourceAssembler {

    public BlockUserForTrustCommand toCommand(Long userId, BlockUserResource resource) {
        return new BlockUserForTrustCommand(userId, resource.moderatorId(), resource.reason());
    }
}