package app.web.rtgtechnologies.rent2go.community_trust.domain.model.services;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Message;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.CloseConversationCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SendMessageCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.StartConversationCommand;

public interface ConversationCommandService {
    Conversation handle(StartConversationCommand command);

    Message handle(SendMessageCommand command);

    Conversation handle(CloseConversationCommand command);
}