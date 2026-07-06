package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest;

import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices.ReservationCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices.ReservationQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationByIdQuery;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.CreateReservationResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ReservationResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.CreateReservationCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.ReservationResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.CancelReservationCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.CancelReservationResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.ModifyReservationCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ModifyReservationResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.UpdateReservationStatusCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.UpdateReservationStatusResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.ConfirmReturnCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ConfirmReturnResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resource.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Tag(name = "Booking & Reservations", description = "Reservation lifecycle operations and renter/owner views")
@RestController
@RequestMapping("/api/v1/reservations")
@AllArgsConstructor
@Validated
public class ReservationController {

    private final ReservationCommandServiceImpl commandService;
    private final ReservationQueryServiceImpl queryService;
    private final CreateReservationCommandFromResourceAssembler commandAssembler;
    private final ReservationResourceFromEntityAssembler resourceAssembler;
    private final CancelReservationCommandFromResourceAssembler cancelAssembler;
    private final ModifyReservationCommandFromResourceAssembler modifyAssembler;
    private final UpdateReservationStatusCommandFromResourceAssembler updateStatusAssembler;
    private final ConfirmReturnCommandFromResourceAssembler confirmReturnAssembler;

    @PostMapping
    @Operation(summary = "Create reservation", description = "Creates a new reservation for a vehicle and renter.")
    public ResponseEntity<ReservationResource> createReservation(@RequestBody @Valid CreateReservationResource resource) {
        CreateReservationCommand command = commandAssembler.toCommand(resource);
        Reservation saved = commandService.handle(command);
        ReservationResource resp = resourceAssembler.toResource(saved);
        return ResponseEntity.created(URI.create("/api/v1/reservations/" + saved.getId())).body(resp);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by id", description = "Returns the reservation details for a single reservation identifier.")
    public ResponseEntity<ReservationResource> getReservationById(@PathVariable Long id) {
        Optional<Reservation> found = queryService.handle(new GetReservationByIdQuery(id));
        return found.map(res -> ResponseEntity.ok(resourceAssembler.toResource(res)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel reservation", description = "Cancels an existing reservation using the cancellation reason and actor data.")
    public ResponseEntity<ReservationResource> cancelReservation(@PathVariable Long id, @RequestBody @Valid CancelReservationResource resource) {
        var command = cancelAssembler.toCommand(id, resource);
        var canceled = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(canceled));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modify reservation", description = "Updates reservation dates or details when the business rules allow it.")
    public ResponseEntity<ReservationResource> modifyReservation(@PathVariable Long id, @RequestBody @Valid ModifyReservationResource resource) {
        var command = modifyAssembler.toCommand(id, resource);
        var updated = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(updated));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update reservation status", description = "Transitions a reservation to another allowed status.")
    public ResponseEntity<ReservationResource> updateReservationStatus(@PathVariable Long id, @RequestBody @Valid UpdateReservationStatusResource resource) {
        var command = updateStatusAssembler.toCommand(id, resource);
        try {
            var updated = commandService.handle(command);
            return ResponseEntity.ok(resourceAssembler.toResource(updated));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm reservation", description = "Transitions a PENDING reservation to CONFIRMED. Can also be triggered automatically by a successful payment webhook.")
    public ResponseEntity<ReservationResource> confirmReservation(@PathVariable Long id) {
        var reservationOpt = queryService.handle(new GetReservationByIdQuery(id));
        if (reservationOpt.isEmpty()) return ResponseEntity.notFound().build();
        var resource = new UpdateReservationStatusResource();
        resource.setActorId(reservationOpt.get().getOwnerId());
        resource.setStatus("CONFIRMED");
        var command = updateStatusAssembler.toCommand(id, resource);
        try {
            var updated = commandService.handle(command);
            return ResponseEntity.ok(resourceAssembler.toResource(updated));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate reservation", description = "Transitions a CONFIRMED reservation to ACTIVE (vehicle picked up).")
    public ResponseEntity<ReservationResource> activateReservation(@PathVariable Long id) {
        var reservationOpt = queryService.handle(new GetReservationByIdQuery(id));
        if (reservationOpt.isEmpty()) return ResponseEntity.notFound().build();
        var resource = new UpdateReservationStatusResource();
        resource.setActorId(reservationOpt.get().getOwnerId());
        resource.setStatus("ACTIVE");
        var command = updateStatusAssembler.toCommand(id, resource);
        try {
            var updated = commandService.handle(command);
            return ResponseEntity.ok(resourceAssembler.toResource(updated));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete reservation", description = "Transitions an ACTIVE reservation to COMPLETED (vehicle returned and confirmed).")
    public ResponseEntity<ReservationResource> completeReservation(@PathVariable Long id) {
        var reservationOpt = queryService.handle(new GetReservationByIdQuery(id));
        if (reservationOpt.isEmpty()) return ResponseEntity.notFound().build();
        var resource = new UpdateReservationStatusResource();
        resource.setActorId(reservationOpt.get().getOwnerId());
        resource.setStatus("COMPLETED");
        var command = updateStatusAssembler.toCommand(id, resource);
        try {
            var updated = commandService.handle(command);
            return ResponseEntity.ok(resourceAssembler.toResource(updated));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/confirm-return")
    @Operation(summary = "Confirm vehicle return", description = "Marks the reservation as returned and records the confirmation details.")
    public ResponseEntity<ReservationResource> confirmReturn(@PathVariable Long id, @RequestBody @Valid ConfirmReturnResource resource) {
        var command = confirmReturnAssembler.toCommand(id, resource);
        var updated = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(updated));
    }

    /**
     * Perf fix (2026-07-06), revised per product requirement: NO pagination — returns the
     * renter's FULL reservation list in one indexed query, sorted with non-terminal
     * reservations (PENDING/CONFIRMED/ACTIVE/RETURN_PENDING/RETURN_CONFIRMED) before terminal
     * ones (COMPLETED/CANCELLED/EXPIRED), most recent start date first within each group. The
     * {@code page}/{@code size} query params are intentionally no longer part of this
     * endpoint's contract (removed, not just ignored) per the explicit requirement that older
     * reservations must never visually outrank more relevant ones due to page slicing.
     * {@code PagedResponse}'s envelope shape is kept for response-body backward compatibility
     * with existing clients (Flutter/Kotlin) that read {@code .content} — {@code page}/{@code size}/
     * {@code totalPages} are now fixed values (1/total-count/1) rather than meaningful pagination
     * state. See ReservationRepository.findAllByRenterIdOrderByPriorityThenStartDateDesc for the
     * single-query implementation (avoids the N+1 fixed separately in
     * ReservationResourceFromEntityAssembler).
     */
    @GetMapping
    @Operation(summary = "List renter reservations", description = "Returns ALL of the renter's reservations (no pagination), optionally filtered by status. Non-terminal reservations (pending/confirmed/active/return in progress) are returned before terminal ones (completed/cancelled/expired), most recent first within each group.")
    public ResponseEntity<PagedResponse<ReservationResource>> listByRenter(
            @RequestParam @Positive(message = "Renter ID must be positive") Long renterId,
            @RequestParam(required = false) String status
    ) {
        var results = queryService.handleRenterListPrioritized(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByRenterQuery(renterId, status));
        List<ReservationResource> content = resourceAssembler.toResources(results);
        return ResponseEntity.ok(new PagedResponse<>(content, 1, content.size(), content.size(), content.isEmpty() ? 0 : 1));
    }

    @GetMapping("/owner")
    @Operation(summary = "List owner reservations", description = "Returns reservations for a vehicle owner, optionally filtered by status. Page starts at 1.")
    public ResponseEntity<PagedResponse<ReservationResource>> listByOwner(
            @RequestParam @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        // Perf fix (2026-07-06): same DB-level pagination fix as listByRenter above.
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerPagedQuery(ownerId, status, page, size));
        return ResponseEntity.ok(toPagedResponse(results, page));
    }

    @GetMapping("/renter/{renterId}/history")
    @Operation(summary = "Get renter history", description = "Returns the completed reservation history for a renter. Page starts at 1.")
    public ResponseEntity<PagedResponse<ReservationResource>> getRenterHistory(
            @PathVariable @Positive(message = "Renter ID must be positive") Long renterId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        // Out of the renter-listing scope changed above (this endpoint is COMPLETED-only, so
        // there is no "non-terminal vs terminal" priority to preserve). Kept paginated as
        // before, but now uses the batched resourceAssembler.toResources(...) so the page's
        // counterparty/vehicle enrichment still avoids the N+1 fixed in this same change.
        var allHistory = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationHistoryByRenterQuery(renterId));
        return ResponseEntity.ok(toPagedResponseInMemory(allHistory, page, size));
    }

    @GetMapping("/owner/paged")
    @Operation(summary = "List owner reservations paged", description = "Returns owner reservations using repository pagination. Page starts at 1 in the API.")
    public ResponseEntity<PagedResponse<ReservationResource>> listByOwnerPaged(
            @RequestParam @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerPagedQuery(ownerId, status, page, size));
        return ResponseEntity.ok(toPagedResponse(results, page));
    }

    /**
     * Perf fix (2026-07-06): builds the response from a real {@link Page} (DB-computed
     * totalElements/totalPages) using {@link ReservationResourceFromEntityAssembler#toResources}
     * so the whole page's counterparty/vehicle enrichment happens in a fixed 3 queries total,
     * instead of up to 5 queries per row.
     */
    private PagedResponse<ReservationResource> toPagedResponse(org.springframework.data.domain.Page<Reservation> source, int page) {
        int safePage = Math.max(1, page);
        List<ReservationResource> content = resourceAssembler.toResources(source.getContent());
        return new PagedResponse<>(content, safePage, source.getSize(), source.getTotalElements(), source.getTotalPages());
    }

    /**
     * In-memory slicing helper, retained only for GET /renter/{renterId}/history (a
     * COMPLETED-only, not-in-scope-for-the-no-pagination-requirement endpoint). Uses the
     * batched {@link ReservationResourceFromEntityAssembler#toResources(List)} on just the
     * sliced page, not the full source list, so this endpoint still avoids the N+1 fixed
     * elsewhere in this change even though it still loads the renter's full completed history
     * before slicing (acceptable here since COMPLETED-only history is typically much smaller
     * than the full mixed-status list GET /api/v1/reservations used to load).
     */
    private PagedResponse<ReservationResource> toPagedResponseInMemory(List<Reservation> source, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        long totalElements = source.size();
        int fromIndex = Math.min((safePage - 1) * safeSize, source.size());
        int toIndex = Math.min(fromIndex + safeSize, source.size());
        List<ReservationResource> content = resourceAssembler.toResources(source.subList(fromIndex, toIndex));
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        return new PagedResponse<>(content, safePage, safeSize, totalElements, totalPages);
    }
}
