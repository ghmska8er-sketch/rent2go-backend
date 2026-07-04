package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest;

import app.web.rtgtechnologies.rent2go.community_trust.application.internal.commandservices.ConversationCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.application.internal.commandservices.ReviewCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices.ConversationQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices.ReviewQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Message;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.TrustReport;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetConversationByIdQuery;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.TrustReportRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.UserReputationRepository;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers.*;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.MessageResource;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.SendMessageResource;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * CommunityTrustControllerTest
 *
 * TS10: participant-only authorization for messaging endpoints must reject non-participants
 * with 403 and continue to allow participants (regression). US42: the new self-service
 * dispute-status endpoint must return only the caller's own disputes and reject access to
 * another user's disputes. All collaborators are mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class CommunityTrustControllerTest {

    @Mock private ReviewCommandServiceImpl commandService;
    @Mock private ReviewQueryServiceImpl queryService;
    @Mock private ConversationCommandServiceImpl conversationCommandService;
    @Mock private ConversationQueryServiceImpl conversationQueryService;
    @Mock private UserReputationRepository userReputationRepository;
    @Mock private TrustReportRepository trustReportRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private SubmitReviewCommandFromResourceAssembler submitAssembler;
    @Mock private FlagReviewCommandFromResourceAssembler flagAssembler;
    @Mock private ModerationActionCommandFromResourceAssembler moderationAssembler;
    @Mock private BlockUserForTrustCommandFromResourceAssembler blockAssembler;
    @Mock private StartConversationCommandFromResourceAssembler startConversationAssembler;
    @Mock private SendMessageCommandFromResourceAssembler sendMessageAssembler;
    @Mock private OpenReservationDisputeCommandFromResourceAssembler disputeAssembler;
    @Mock private ReviewResourceFromEntityAssembler reviewAssembler;
    @Mock private ConversationResourceFromEntityAssembler conversationAssembler;
    @Mock private MessageResourceFromEntityAssembler messageAssembler;

    private CommunityTrustController controller;

    @BeforeEach
    void setUp() {
        controller = new CommunityTrustController(
                commandService,
                queryService,
                conversationCommandService,
                conversationQueryService,
                userReputationRepository,
                trustReportRepository,
                currentUserService,
                submitAssembler,
                flagAssembler,
                moderationAssembler,
                blockAssembler,
                startConversationAssembler,
                sendMessageAssembler,
                disputeAssembler,
                reviewAssembler,
                conversationAssembler,
                messageAssembler
        );
    }

    @Test
    void sendMessage_returnsForbidden_whenCallerIsNotAParticipant() {
        Conversation conversation = Conversation.start(1L, 2L, 5L, 9L, "subject");
        when(conversationQueryService.handle(new GetConversationByIdQuery(100L))).thenReturn(Optional.of(conversation));
        when(currentUserService.isCurrentUserAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(999L)); // not owner(1) nor renter(2)

        var response = controller.sendMessage(100L, new SendMessageResource(1L, "hello"));

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void sendMessage_returnsCreated_whenCallerIsAParticipant() {
        Conversation conversation = Conversation.start(1L, 2L, 5L, 9L, "subject");
        when(conversationQueryService.handle(new GetConversationByIdQuery(100L))).thenReturn(Optional.of(conversation));
        when(currentUserService.isCurrentUserAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(1L)); // matches ownerId

        var resource = new SendMessageResource(1L, "hello");
        var command = sendMessageAssemblerToCommandStub();
        when(sendMessageAssembler.toCommand(100L, resource)).thenReturn(command);
        Message saved = org.mockito.Mockito.mock(Message.class);
        when(saved.getId()).thenReturn(500L);
        when(conversationCommandService.handle(command)).thenReturn(saved);
        when(messageAssembler.toResource(saved)).thenReturn(new MessageResource(500L, 100L, 1L, "hello", null, null, null, null, null, null));

        var response = controller.sendMessage(100L, resource);

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void getMessagesByConversation_returnsForbidden_whenCallerIsNotAParticipant() {
        Conversation conversation = Conversation.start(1L, 2L, 5L, 9L, "subject");
        when(conversationQueryService.handle(new GetConversationByIdQuery(100L))).thenReturn(Optional.of(conversation));
        when(currentUserService.isCurrentUserAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(999L));

        var response = controller.getMessagesByConversation(100L);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getMessagesByConversation_returnsOk_whenCallerIsAParticipant() {
        Conversation conversation = Conversation.start(1L, 2L, 5L, 9L, "subject");
        when(conversationQueryService.handle(new GetConversationByIdQuery(100L))).thenReturn(Optional.of(conversation));
        when(currentUserService.isCurrentUserAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(2L)); // matches renterId
        when(conversationQueryService.handle(new app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetMessagesByConversationQuery(100L)))
                .thenReturn(List.of());

        var response = controller.getMessagesByConversation(100L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getMyDisputes_returnsOnlyCallersOwnDisputes() {
        when(currentUserService.isOwnerOrAdmin(7L)).thenReturn(true);
        TrustReport report = TrustReport.openDispute(50L, 8L, 7L, "issue");
        when(trustReportRepository.findAllByReporterIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(report));

        var response = controller.getMyDisputes(7L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(7L, response.getBody().get(0).reporterId());
    }

    @Test
    void getMyDisputes_returnsForbidden_whenCallerRequestsAnotherUsersDisputes() {
        when(currentUserService.isOwnerOrAdmin(7L)).thenReturn(false);

        var response = controller.getMyDisputes(7L);

        assertEquals(403, response.getStatusCode().value());
    }

    private app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SendMessageCommand sendMessageAssemblerToCommandStub() {
        return new app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SendMessageCommand(100L, 1L, "hello");
    }
}
