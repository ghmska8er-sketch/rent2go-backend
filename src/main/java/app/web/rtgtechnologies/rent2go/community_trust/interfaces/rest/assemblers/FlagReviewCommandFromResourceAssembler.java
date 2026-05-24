package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.FlagReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.FlagReviewResource;
import org.springframework.stereotype.Component;

@Component
public class FlagReviewCommandFromResourceAssembler {

    public FlagReviewCommand toCommand(Long reviewId, FlagReviewResource resource) {
        return new FlagReviewCommand(reviewId, resource.reporterId(), resource.reason());
    }
}