package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResource(
    Long id,
    String reservationCode,
    Long vehicleId,
    Long renterId,
    Long ownerId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalAmount,
    String status,
    LocalDateTime pickupConfirmedAt,
    LocalDateTime returnConfirmedAt,
    String pickupLocation,
    String returnLocation,
    String coveragePlan,
    java.util.List<String> pickupPhotos,
    java.util.List<String> returnPhotos,
    String damageReport
) {}
