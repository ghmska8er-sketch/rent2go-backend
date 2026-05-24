package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.ApproveReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.RejectReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ModerationActionResource;
import org.springframework.stereotype.Component;

@Component
public class ModerationActionCommandFromResourceAssembler {

    public ApproveReviewCommand toApproveCommand(Long reviewId, ModerationActionResource resource) {
        return new ApproveReviewCommand(reviewId, resource.moderatorId(), resource.reason());
    }

    public RejectReviewCommand toRejectCommand(Long reviewId, ModerationActionResource resource) {
        return new RejectReviewCommand(reviewId, resource.moderatorId(), resource.reason());
    }
}