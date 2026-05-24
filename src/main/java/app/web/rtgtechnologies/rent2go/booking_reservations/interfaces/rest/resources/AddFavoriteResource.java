package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

public record AddFavoriteResource(
        @NotNull(message = "User ID is required") Long userId,
        @NotNull(message = "Vehicle ID is required") Long vehicleId
) {}
