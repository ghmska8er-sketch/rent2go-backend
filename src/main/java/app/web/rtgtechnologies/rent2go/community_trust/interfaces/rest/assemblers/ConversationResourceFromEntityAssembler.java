package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ConversationResource;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TS18 — extended to embed a nested counterparty (owner/renter) object, mirroring the
 * community_trust -> iam cross-context read pattern already established in
 * ReviewCommandServiceImpl. Additive only: ownerId/renterId fields are unchanged.
 */
@Component
@RequiredArgsConstructor
public class ConversationResourceFromEntityAssembler {

    private static final String NO_NAME_ON_FILE = "Usuario sin nombre registrado";

    private final UserRepository userRepository;

    public ConversationResource toResource(Conversation conversation) {
        return new ConversationResource(
            conversation.getId(),
            conversation.getOwnerId(),
            conversation.getRenterId(),
            conversation.getVehicleId(),
            conversation.getReservationId(),
            conversation.getSubject(),
            conversation.getStatus() == null ? null : conversation.getStatus().name(),
            conversation.getLastMessageAt() == null ? null : conversation.getLastMessageAt().toString(),
            conversation.getLastMessagePreview(),
            conversation.getCreatedAt() == null ? null : conversation.getCreatedAt().toString(),
            conversation.getUpdatedAt() == null ? null : conversation.getUpdatedAt().toString(),
            toCounterparty(conversation.getOwnerId()),
            toCounterparty(conversation.getRenterId())
        );
    }

    private CounterpartyResource toCounterparty(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
            .map(this::toCounterpartyResource)
            .orElse(new CounterpartyResource(userId, NO_NAME_ON_FILE, false));
    }

    private CounterpartyResource toCounterpartyResource(User user) {
        String fullName = user.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = NO_NAME_ON_FILE;
        }
        return new CounterpartyResource(user.getId(), fullName, user.isKycVerified());
    }
}