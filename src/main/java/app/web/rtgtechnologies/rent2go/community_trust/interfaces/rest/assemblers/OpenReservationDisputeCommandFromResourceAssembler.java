package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.OpenReservationDisputeCommand;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.OpenReservationDisputeResource;

public class OpenReservationDisputeCommandFromResourceAssembler {

    public OpenReservationDisputeCommand toCommand(Long reservationId, OpenReservationDisputeResource resource) {
        return new OpenReservationDisputeCommand(reservationId, resource.reporterId(), resource.reason());
    }
}