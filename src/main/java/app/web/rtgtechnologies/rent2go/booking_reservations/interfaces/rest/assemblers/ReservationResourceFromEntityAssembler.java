package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ReservationResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers.CounterpartyResourceAssembler;
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
 * split KYC verification booleans + profile photo on the nested counterparty. The
 * name/KYC-join logic itself now lives in {@link CounterpartyResourceAssembler} (single
 * source of truth, shared with {@code ConversationResourceFromEntityAssembler} and the
 * new vehicle owner-summary endpoint) — this class only delegates to it. Since this
 * assembler only ever processes a single reservation at a time (no list-batching entry
 * point exists in this class), the "batched lookup" risk called out in the BRD's R-001 does
 * not apply here — at most 2 distinct counterparty IDs (renter, owner) are looked up per
 * call, identical in shape to the existing per-reservation UserRepository lookups above.
 */
@Component
@RequiredArgsConstructor
public class ReservationResourceFromEntityAssembler {

    private final VehicleRepository vehicleRepository;
    private final CounterpartyResourceAssembler counterpartyResourceAssembler;

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
            counterpartyResourceAssembler.toCounterparty(r.getRenterId()),
            counterpartyResourceAssembler.toCounterparty(r.getOwnerId()),
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
}
