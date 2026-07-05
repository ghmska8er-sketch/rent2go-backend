package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ReservationResource;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TS18 — extended to embed a nested counterparty (renter/owner) object, mirroring the
 * community_trust -> iam cross-context read pattern already established in
 * ReviewCommandServiceImpl. Additive only: renterId/ownerId fields are unchanged.
 *
 * Sprint 5 (US76/TS23, BRD-2026-07-05-Reservation-Vehicle-Counterparty-Enrichment.md) —
 * further extended to embed {@code vehicleImage} (from {@code Vehicle.primaryImageUrl}) and
 * split KYC verification booleans + profile photo on the nested counterparty, joined from
 * the existing {@code KycApplication} table (most-recent record per user). Since this
 * assembler only ever processes a single reservation at a time (no list-batching entry
 * point exists in this class), the "batched lookup" risk called out in the BRD's R-001 does
 * not apply here — at most 2 distinct counterparty IDs (renter, owner) are looked up per
 * call, identical in shape to the existing per-reservation UserRepository lookups above.
 */
@Component
@RequiredArgsConstructor
public class ReservationResourceFromEntityAssembler {

    private static final String NO_NAME_ON_FILE = "Usuario sin nombre registrado";
    private static final String KYC_APPROVED = "APPROVED";

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final KycApplicationRepository kycApplicationRepository;

    public ReservationResource toResource(Reservation r) {
        return new ReservationResource(
            r.getId(),
            r.getReservationCode().getCode(),
            r.getVehicleId(),
            r.getRenterId(),
            r.getOwnerId(),
            r.getDateRange().getStartDate(),
            r.getDateRange().getEndDate(),
            r.getTotalAmount(),
            r.getStatus().getStatus(),
            r.getPickupConfirmedAt(),
            r.getReturnConfirmedAt(),
            r.getPickupLocation(),
            r.getReturnLocation(),
            r.getCoveragePlan(),
            r.getPickupPhotos(),
            r.getReturnPhotos(),
            r.getDamageReport(),
            toCounterparty(r.getRenterId()),
            toCounterparty(r.getOwnerId()),
            toVehicleImage(r.getVehicleId())
        );
    }

    private String toVehicleImage(Long vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        return vehicleRepository.findById(vehicleId)
            .map(app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle::getPrimaryImageUrl)
            .orElse(null);
    }

    private CounterpartyResource toCounterparty(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
            .map(this::toCounterpartyResource)
            .orElse(new CounterpartyResource(userId, NO_NAME_ON_FILE, false, false, false, null));
    }

    private CounterpartyResource toCounterpartyResource(User user) {
        String fullName = user.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = NO_NAME_ON_FILE;
        }
        boolean verified = isKycApplicationApproved(user.getId());
        return new CounterpartyResource(
            user.getId(),
            fullName,
            user.isKycVerified(),
            verified,
            verified,
            user.getProfileImageUrl()
        );
    }

    /**
     * Derives dniVerified/licenseVerified from the counterparty's most recent
     * {@code KycApplication} record. {@code KycApplication} does not (yet) track DNI and
     * driver's-license status as two independently-tracked fields — both split badges are
     * therefore identical today, both sourced from the one existing collapsed
     * {@code KycApplication.status}, per the pre-resolved product decision (zero new tables,
     * zero new per-document status columns). Returns false, never throws, when no
     * application exists on file.
     */
    private boolean isKycApplicationApproved(Long userId) {
        if (userId == null) {
            return false;
        }
        return kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
            .map(app -> KYC_APPROVED.equalsIgnoreCase(app.getStatus()))
            .orElse(false);
    }
}
