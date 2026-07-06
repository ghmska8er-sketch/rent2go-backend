package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ConversationResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers.CounterpartyResourceAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TS18 — extended to embed a nested counterparty (owner/renter) object, mirroring the
 * community_trust -> iam cross-context read pattern already established in
 * ReviewCommandServiceImpl. Additive only: ownerId/renterId fields are unchanged.
 *
 * Sprint 5 (US76/TS23) — extended to also populate the counterparty's split KYC booleans and
 * profile photo. The name/KYC-join logic itself now lives in
 * {@link CounterpartyResourceAssembler} (single source of truth, shared with
 * {@code ReservationResourceFromEntityAssembler} and the vehicle owner-summary endpoint) —
 * this class only delegates to it.
 */
@Component
@RequiredArgsConstructor
public class ConversationResourceFromEntityAssembler {

    private final CounterpartyResourceAssembler counterpartyResourceAssembler;

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
            counterpartyResourceAssembler.toCounterparty(conversation.getOwnerId()),
            counterpartyResourceAssembler.toCounterparty(conversation.getRenterId())
        );
    }
}