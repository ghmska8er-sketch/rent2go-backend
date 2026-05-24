package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record StartConversationCommand(
    Long ownerId,
    Long renterId,
    Long vehicleId,
    Long reservationId,
    String subject
) {
}