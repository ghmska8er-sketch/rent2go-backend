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
import java.util.function.Function;

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

    @GetMapping
    @Operation(summary = "List renter reservations", description = "Returns the renter's reservations, optionally filtered by status. Page starts at 1.")
    public ResponseEntity<PagedResponse<ReservationResource>> listByRenter(
            @RequestParam @Positive(message = "Renter ID must be positive") Long renterId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByRenterQuery(renterId, status));
        return ResponseEntity.ok(toPagedResponse(results, page, size, resourceAssembler::toResource));
    }

    @GetMapping("/owner")
    @Operation(summary = "List owner reservations", description = "Returns reservations for a vehicle owner, optionally filtered by status. Page starts at 1.")
    public ResponseEntity<PagedResponse<ReservationResource>> listByOwner(
            @RequestParam @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerQuery(ownerId, status));
        return ResponseEntity.ok(toPagedResponse(results, page, size, resourceAssembler::toResource));
    }

    @GetMapping("/renter/{renterId}/history")
    @Operation(summary = "Get renter history", description = "Returns the completed reservation history for a renter. Page starts at 1.")
    public ResponseEntity<PagedResponse<ReservationResource>> getRenterHistory(
            @PathVariable @Positive(message = "Renter ID must be positive") Long renterId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationHistoryByRenterQuery(renterId));
        return ResponseEntity.ok(toPagedResponse(results, page, size, resourceAssembler::toResource));
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
        var content = results.getContent().stream().map(resourceAssembler::toResource).toList();
        return ResponseEntity.ok(new PagedResponse<>(content, results.getNumber(), results.getSize(), results.getTotalElements(), results.getTotalPages()));
    }

    private <T, R> PagedResponse<R> toPagedResponse(List<T> source, int page, int size, Function<T, R> mapper) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        long totalElements = source.size();
        int fromIndex = Math.min((safePage - 1) * safeSize, source.size());
        int toIndex = Math.min(fromIndex + safeSize, source.size());
        List<R> content = source.subList(fromIndex, toIndex).stream().map(mapper).toList();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        return new PagedResponse<>(content, safePage, safeSize, totalElements, totalPages);
    }
}
