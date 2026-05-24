package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddFavoriteResource(
        @NotNull(message = "User ID is required") @Positive(message = "User ID must be positive") Long userId,
        @NotNull(message = "Vehicle ID is required") @Positive(message = "Vehicle ID must be positive") Long vehicleId
) {}
