package app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands;

public record OpenReservationDisputeCommand(
    Long reservationId,
    Long reporterId,
    String reason
) {
}