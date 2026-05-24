package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SubmitReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.SubmitReviewResource;
import org.springframework.stereotype.Component;

@Component
public class SubmitReviewCommandFromResourceAssembler {

    public SubmitReviewCommand toCommand(SubmitReviewResource resource) {
        return new SubmitReviewCommand(
            resource.reservationId(),
            resource.vehicleId(),
            resource.reviewerId(),
            resource.reviewedUserId(),
            resource.category(),
            resource.rating(),
            resource.comment()
        );
    }
}