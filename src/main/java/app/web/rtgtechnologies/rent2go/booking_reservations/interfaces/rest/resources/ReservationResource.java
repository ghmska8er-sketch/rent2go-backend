package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    String damageReport,
    // TS18 — additive nested counterparty objects; renterId/ownerId are kept unchanged above.
    @JsonProperty("renter")
    CounterpartyResource renter,
    @JsonProperty("owner")
    CounterpartyResource owner,
    // Sprint 5 (US76/TS23) — additive: the vehicle's catalog photo, sourced from
    // Vehicle.primaryImageUrl. Null when the vehicle has no registered image. Distinct from
    // pickupPhotos/returnPhotos above (check-in condition photos, a separate concept).
    @JsonProperty("vehicle_image")
    String vehicleImage
) {}
