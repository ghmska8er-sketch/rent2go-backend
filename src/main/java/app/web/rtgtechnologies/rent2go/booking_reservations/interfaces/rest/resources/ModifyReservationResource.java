package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ModifyReservationResource(
	@NotNull(message = "Start date is required") LocalDate startDate,
	@NotNull(message = "End date is required") LocalDate endDate,
	@NotNull(message = "Requester ID is required") @Positive(message = "Requester ID must be positive") Long requestedBy
) {}
