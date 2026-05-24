package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CancelReservationResource(
	@NotNull(message = "Requester ID is required") @Positive(message = "Requester ID must be positive") Long requestedById,
	String reason
) {}
