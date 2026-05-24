package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

public record ConversationResource(
    Long id,
    Long ownerId,
    Long renterId,
    Long vehicleId,
    Long reservationId,
    String subject,
    String status,
    String lastMessageAt,
    String lastMessagePreview,
    String createdAt,
    String updatedAt
) {
}