package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest;

import app.web.rtgtechnologies.rent2go.community_trust.application.internal.commandservices.ReviewCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices.ReviewQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.ApproveReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.BlockUserForTrustCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.FlagReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.RejectReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SubmitReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetAverageRatingQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByVehicleQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetUserReputationQuery;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.BlockUserForTrustCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.FlagReviewCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.ModerationActionCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.ReviewResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.SubmitReviewCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.BlockUserResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.FlagReviewResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ModerationActionResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ReviewResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.SubmitReviewResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.UserReputationResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.VehicleRatingResource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/community-trust")
@RequiredArgsConstructor
public class CommunityTrustController {

    private final ReviewCommandServiceImpl commandService;
    private final ReviewQueryServiceImpl queryService;
    private final SubmitReviewCommandFromResourceAssembler submitAssembler;
    private final FlagReviewCommandFromResourceAssembler flagAssembler;
    private final ModerationActionCommandFromResourceAssembler moderationAssembler;
    private final BlockUserForTrustCommandFromResourceAssembler blockAssembler;
    private final ReviewResourceFromEntityAssembler reviewAssembler;

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResource> submitReview(@Valid @RequestBody SubmitReviewResource resource) {
        SubmitReviewCommand command = submitAssembler.toCommand(resource);
        var saved = commandService.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/community-trust/reviews/" + saved.getId()))
            .body(reviewAssembler.toResource(saved));
    }

    @PostMapping("/reviews/{reviewId}/flag")
    public ResponseEntity<ReviewResource> flagReview(@PathVariable Long reviewId, @Valid @RequestBody FlagReviewResource resource) {
        FlagReviewCommand command = flagAssembler.toCommand(reviewId, resource);
        var saved = commandService.handle(command);
        return ResponseEntity.ok(reviewAssembler.toResource(saved));
    }

    @PostMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<ReviewResource> approveReview(@PathVariable Long reviewId, @Valid @RequestBody ModerationActionResource resource) {
        ApproveReviewCommand command = moderationAssembler.toApproveCommand(reviewId, resource);
        var saved = commandService.handle(command);
        return ResponseEntity.ok(reviewAssembler.toResource(saved));
    }

    @PostMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<ReviewResource> rejectReview(@PathVariable Long reviewId, @Valid @RequestBody ModerationActionResource resource) {
        RejectReviewCommand command = moderationAssembler.toRejectCommand(reviewId, resource);
        var saved = commandService.handle(command);
        return ResponseEntity.ok(reviewAssembler.toResource(saved));
    }

    @GetMapping("/reviews/vehicle/{vehicleId}")
    public ResponseEntity<List<ReviewResource>> getReviewsByVehicle(@PathVariable Long vehicleId) {
        var results = queryService.handle(new GetReviewsByVehicleQuery(vehicleId));
        return ResponseEntity.ok(results.stream().map(reviewAssembler::toResource).toList());
    }

    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<List<ReviewResource>> getReviewsByUser(@PathVariable Long userId) {
        var results = queryService.handle(new GetReviewsByUserQuery(userId));
        return ResponseEntity.ok(results.stream().map(reviewAssembler::toResource).toList());
    }

    @GetMapping("/users/{userId}/reputation")
    public ResponseEntity<UserReputationResource> getUserReputation(@PathVariable Long userId) {
        return queryService.handle(new GetUserReputationQuery(userId))
            .map(rep -> ResponseEntity.ok(new UserReputationResource(
                rep.getUserId(),
                rep.getApprovedReviewCount(),
                rep.getAverageRating(),
                rep.getTrustScore(),
                rep.isBlocked(),
                rep.getLastModerationReason()
            )))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicles/{vehicleId}/rating")
    public ResponseEntity<VehicleRatingResource> getVehicleRating(@PathVariable Long vehicleId) {
        var reviews = queryService.handle(new GetReviewsByVehicleQuery(vehicleId));
        var approved = reviews.stream().filter(review -> review.getStatus() != null && review.getStatus().isApproved()).toList();
        if (approved.isEmpty()) {
            return ResponseEntity.ok(new VehicleRatingResource(vehicleId, java.math.BigDecimal.ZERO, 0));
        }

        var average = queryService.handle(new GetAverageRatingQuery(vehicleId)).orElse(java.math.BigDecimal.ZERO);
        return ResponseEntity.ok(new VehicleRatingResource(vehicleId, average, approved.size()));
    }

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId, @Valid @RequestBody BlockUserResource resource) {
        BlockUserForTrustCommand command = blockAssembler.toCommand(userId, resource);
        commandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}