package app.web.rtgtechnologies.rent2go.community_trust.domain.model.services;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Message;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetConversationByIdQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetConversationsByUserQuery;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.queries.GetMessagesByConversationQuery;

import java.util.List;
import java.util.Optional;

public interface ConversationQueryService {
    Optional<Conversation> handle(GetConversationByIdQuery query);

    List<Conversation> handle(GetConversationsByUserQuery query);

    List<Message> handle(GetMessagesByConversationQuery query);
}