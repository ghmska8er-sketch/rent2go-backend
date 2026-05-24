package app.web.rtgtechnologies.rent2go.community_trust.domain.model.services;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Review;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.ApproveReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.BlockUserForTrustCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.FlagReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.RejectReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SubmitReviewCommand;

public interface ReviewCommandService {

    Review handle(SubmitReviewCommand command);

    Review handle(FlagReviewCommand command);

    Review handle(ApproveReviewCommand command);

    Review handle(RejectReviewCommand command);

    void handle(BlockUserForTrustCommand command);
}