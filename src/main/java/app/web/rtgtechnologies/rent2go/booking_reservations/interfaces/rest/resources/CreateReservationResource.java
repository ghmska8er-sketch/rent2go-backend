package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReservationResource(
    Long vehicleId,
    Long renterId,
    Long ownerId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalAmount
) {}
