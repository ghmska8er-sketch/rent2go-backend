package app.web.rtgtechnologies.rent2go.community_trust.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Message;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.CloseConversationCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SendMessageCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.StartConversationCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.services.ConversationCommandService;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.ConversationRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationCommandServiceImpl implements ConversationCommandService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public Conversation handle(StartConversationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command is required");
        }

        var conversation = Conversation.start(
            command.ownerId(),
            command.renterId(),
            command.vehicleId(),
            command.reservationId(),
            command.subject()
        );

        return conversationRepository.save(conversation);
    }

    @Override
    public Message handle(SendMessageCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command is required");
        }

        var conversation = conversationRepository.findById(command.conversationId())
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + command.conversationId()));

        if (!conversation.belongsTo(command.senderId())) {
            throw new IllegalArgumentException("Sender does not belong to the conversation");
        }

        var message = Message.send(command.conversationId(), command.senderId(), command.content());
        var savedMessage = messageRepository.save(message);
        conversation.touchMessage(savedMessage.getContent());
        conversationRepository.save(conversation);
        return savedMessage;
    }

    @Override
    public Conversation handle(CloseConversationCommand command) {
        var conversation = conversationRepository.findById(command.conversationId())
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + command.conversationId()));

        if (!conversation.belongsTo(command.userId())) {
            throw new IllegalArgumentException("User does not belong to the conversation");
        }

        conversation.close();
        return conversationRepository.save(conversation);
    }
}