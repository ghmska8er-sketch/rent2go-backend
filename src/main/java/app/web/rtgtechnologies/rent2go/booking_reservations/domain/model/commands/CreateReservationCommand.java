package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CreateReservationCommand
 *
 * Command to create a new reservation in the booking context.
 */
public record CreateReservationCommand(
    Long vehicleId,
    Long renterId,
    Long ownerId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalAmount
) {
}