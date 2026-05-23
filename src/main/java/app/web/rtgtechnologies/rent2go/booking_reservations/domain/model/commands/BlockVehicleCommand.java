package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands;

import java.time.LocalDate;

public record BlockVehicleCommand(Long vehicleId, LocalDate startDate, LocalDate endDate, Long requestedBy) {}
