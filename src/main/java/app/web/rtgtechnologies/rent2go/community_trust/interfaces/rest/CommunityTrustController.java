package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest;

import app.web.rtgtechnologies.rent2go.community_trust.application.internal.commandservices.ReviewCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.application.internal.commandservices.ConversationCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices.ConversationQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices.ReviewQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.CloseConversationCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.ApproveReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.BlockUserForTrustCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.FlagReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.OpenReservationDisputeCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.RejectReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SendMessageCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.StartConversationCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SubmitReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetConversationByIdQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetCommunityTrustDashboardQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetConversationsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetAverageRatingQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetMessagesByConversationQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetReviewsByVehicleQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetUserReputationQuery;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.ConversationResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.BlockUserForTrustCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.MessageResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.FlagReviewCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.ModerationActionCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.OpenReservationDisputeCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.SendMessageCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.StartConversationCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.ReviewResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.SubmitReviewCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ConversationResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.CommunityTrustDashboardResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.BlockUserResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.MessageResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.FlagReviewResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ModerationActionResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.OpenReservationDisputeResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ReviewResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.SendMessageResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.StartConversationResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.TrustReportResource;
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
    private final ConversationCommandServiceImpl conversationCommandService;
    private final ConversationQueryServiceImpl conversationQueryService;
    private final SubmitReviewCommandFromResourceAssembler submitAssembler;
    private final FlagReviewCommandFromResourceAssembler flagAssembler;
    private final ModerationActionCommandFromResourceAssembler moderationAssembler;
    private final BlockUserForTrustCommandFromResourceAssembler blockAssembler;
    private final StartConversationCommandFromResourceAssembler startConversationAssembler;
    private final SendMessageCommandFromResourceAssembler sendMessageAssembler;
    private final OpenReservationDisputeCommandFromResourceAssembler disputeAssembler;
    private final ReviewResourceFromEntityAssembler reviewAssembler;
    private final ConversationResourceFromEntityAssembler conversationAssembler;
    private final MessageResourceFromEntityAssembler messageAssembler;

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

    @GetMapping("/dashboard")
    public ResponseEntity<CommunityTrustDashboardResource> getDashboard() {
        var dashboard = queryService.handle(new GetCommunityTrustDashboardQuery());
        return ResponseEntity.ok(new CommunityTrustDashboardResource(
            dashboard.totalReviews(),
            dashboard.approvedReviews(),
            dashboard.rejectedReviews(),
            dashboard.flaggedReviews(),
            dashboard.openReports(),
            dashboard.resolvedReports(),
            dashboard.blockedUsers(),
            dashboard.activeConversations(),
            dashboard.totalMessages(),
            dashboard.averageTrustScore()
        ));
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

    @PostMapping("/reservations/{reservationId}/disputes")
    public ResponseEntity<TrustReportResource> openReservationDispute(@PathVariable Long reservationId, @Valid @RequestBody OpenReservationDisputeResource resource) {
        OpenReservationDisputeCommand command = disputeAssembler.toCommand(reservationId, resource);
        var saved = commandService.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/community-trust/reservations/" + reservationId + "/disputes/" + saved.getId()))
            .body(new TrustReportResource(
                saved.getId(),
                saved.getSubjectType() == null ? null : saved.getSubjectType().name(),
                saved.getSubjectId(),
                saved.getReservationId(),
                saved.getReviewId(),
                saved.getReportedUserId(),
                saved.getReporterId(),
                saved.getReason(),
                saved.getStatus() == null ? null : saved.getStatus().name(),
                saved.getModerationNote(),
                saved.getCreatedAt() == null ? null : saved.getCreatedAt().toString(),
                saved.getUpdatedAt() == null ? null : saved.getUpdatedAt().toString()
            ));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationResource> startConversation(@Valid @RequestBody StartConversationResource resource) {
        StartConversationCommand command = startConversationAssembler.toCommand(resource);
        var saved = conversationCommandService.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/community-trust/conversations/" + saved.getId()))
            .body(conversationAssembler.toResource(saved));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResource> getConversation(@PathVariable Long conversationId) {
        return conversationQueryService.handle(new GetConversationByIdQuery(conversationId))
            .map(conversation -> ResponseEntity.ok(conversationAssembler.toResource(conversation)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/conversations")
    public ResponseEntity<List<ConversationResource>> getConversationsByUser(@PathVariable Long userId) {
        var results = conversationQueryService.handle(new GetConversationsByUserQuery(userId));
        return ResponseEntity.ok(results.stream().map(conversationAssembler::toResource).toList());
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResource> sendMessage(@PathVariable Long conversationId, @Valid @RequestBody SendMessageResource resource) {
        SendMessageCommand command = sendMessageAssembler.toCommand(conversationId, resource);
        var saved = conversationCommandService.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/community-trust/conversations/" + conversationId + "/messages/" + saved.getId()))
            .body(messageAssembler.toResource(saved));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResource>> getMessagesByConversation(@PathVariable Long conversationId) {
        var results = conversationQueryService.handle(new GetMessagesByConversationQuery(conversationId));
        return ResponseEntity.ok(results.stream().map(messageAssembler::toResource).toList());
    }

    @PostMapping("/conversations/{conversationId}/close")
    public ResponseEntity<ConversationResource> closeConversation(@PathVariable Long conversationId, @RequestParam Long userId) {
        var saved = conversationCommandService.handle(new CloseConversationCommand(conversationId, userId));
        return ResponseEntity.ok(conversationAssembler.toResource(saved));
    }
}