package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ReservationResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers.CounterpartyResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * new vehicle owner-summary endpoint) — this class only delegates to it.
 *
 * Perf fix (2026-07-06): added {@link #toResources(List)}, a batched variant of
 * {@link #toResource(Reservation)} for list endpoints (GET /api/v1/reservations,
 * /owner, /owner/paged, /renter/{id}/history). The single-item {@code toResource} previously
 * fired 1 vehicle lookup + 2 counterparty lookups (renter, owner; each itself 2 queries) = 5
 * queries PER reservation row, which for a 50-row page meant ~250 extra sequential round-trips
 * to the DB — the dominant cause of GET /api/v1/reservations taking 30-60s in production.
 * {@code toResources} instead resolves all distinct vehicle ids and all distinct counterparty
 * ids for the whole page in 3 queries total, regardless of page size. {@code toResource} is
 * kept for the remaining single-reservation call sites (create/cancel/modify/status endpoints)
 * where the batching has no benefit.
 */
@Component
@RequiredArgsConstructor
public class ReservationResourceFromEntityAssembler {

    private final VehicleRepository vehicleRepository;
    private final CounterpartyResourceAssembler counterpartyResourceAssembler;

    public ReservationResource toResource(Reservation r) {
        CounterpartyResource renter = counterpartyResourceAssembler.toCounterparty(r.getRenterId());
        CounterpartyResource owner = counterpartyResourceAssembler.toCounterparty(r.getOwnerId());
        String vehicleImage = r.getVehicleId() == null ? null
            : vehicleRepository.findById(r.getVehicleId()).map(Vehicle::getPrimaryImageUrl).orElse(null);
        return toResource(r, renter, owner, vehicleImage);
    }

    /**
     * Batched conversion for list endpoints. Resolves vehicles and counterparties for the
     * entire input list with 3 queries total (1 vehicle batch lookup + the 2 queries inside
     * {@link CounterpartyResourceAssembler#toCounterparties(java.util.Collection)}), instead of
     * up to 5 queries per row.
     */
    public List<ReservationResource> toResources(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return List.of();
        }

        var vehicleIds = reservations.stream()
            .map(Reservation::getVehicleId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, String> vehicleImageByVehicleId = vehicleRepository.findAllById(vehicleIds).stream()
            .collect(Collectors.toMap(Vehicle::getId, v -> v.getPrimaryImageUrl() == null ? "" : v.getPrimaryImageUrl()));

        var counterpartyIds = reservations.stream()
            .flatMap(r -> java.util.stream.Stream.of(r.getRenterId(), r.getOwnerId()))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, CounterpartyResource> counterpartiesById = counterpartyResourceAssembler.toCounterparties(counterpartyIds);

        return reservations.stream()
            .map(r -> {
                CounterpartyResource renter = counterpartiesById.get(r.getRenterId());
                CounterpartyResource owner = counterpartiesById.get(r.getOwnerId());
                String vehicleImage = vehicleImageByVehicleId.get(r.getVehicleId());
                // Vehicles that don't resolve to a primaryImageUrl are stored as "" above to
                // distinguish "vehicle found, no image" from "vehicle id absent from the batch
                // result" — normalize back to null for the response payload either way.
                return toResource(r, renter, owner, vehicleImage == null || vehicleImage.isEmpty() ? null : vehicleImage);
            })
            .toList();
    }

    private ReservationResource toResource(Reservation r, CounterpartyResource renter, CounterpartyResource owner, String vehicleImage) {
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
            renter,
            owner,
            vehicleImage
        );
    }
}
