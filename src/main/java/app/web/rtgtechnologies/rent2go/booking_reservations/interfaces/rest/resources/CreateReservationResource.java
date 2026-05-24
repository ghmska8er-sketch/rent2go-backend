package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReservationResource(
    @NotNull(message = "Vehicle ID is required") @Positive(message = "Vehicle ID must be positive") Long vehicleId,
    @NotNull(message = "Renter ID is required") @Positive(message = "Renter ID must be positive") Long renterId,
    @NotNull(message = "Owner ID is required") @Positive(message = "Owner ID must be positive") Long ownerId,
    @NotNull(message = "Start date is required") LocalDate startDate,
    @NotNull(message = "End date is required") LocalDate endDate,
    @NotNull(message = "Total amount is required") @DecimalMin(value = "0.01", message = "Total amount must be greater than 0") BigDecimal totalAmount
) {}
