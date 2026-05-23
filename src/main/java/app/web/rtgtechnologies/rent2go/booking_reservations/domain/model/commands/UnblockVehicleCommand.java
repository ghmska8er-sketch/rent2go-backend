package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands;

public record UnblockVehicleCommand(Long blockId, Long requestedBy) {}
