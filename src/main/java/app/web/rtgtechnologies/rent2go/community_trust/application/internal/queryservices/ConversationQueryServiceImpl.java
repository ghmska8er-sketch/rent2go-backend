package app.web.rtgtechnologies.rent2go.community_trust.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Message;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetConversationByIdQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetConversationsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetMessagesByConversationQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.services.ConversationQueryService;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.ConversationRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationQueryServiceImpl implements ConversationQueryService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public Optional<Conversation> handle(GetConversationByIdQuery query) {
        return conversationRepository.findById(query.conversationId());
    }

    @Override
    public List<Conversation> handle(GetConversationsByUserQuery query) {
        return conversationRepository.findAllByOwnerIdOrRenterIdOrderByUpdatedAtDesc(query.userId(), query.userId());
    }

    @Override
    public List<Message> handle(GetMessagesByConversationQuery query) {
        return messageRepository.findAllByConversationIdOrderByCreatedAtAsc(query.conversationId());
    }
}