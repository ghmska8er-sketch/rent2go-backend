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
    public ResponseEntity<ReservationResource> createReservation(@RequestBody @Valid CreateReservationResource resource) {
        CreateReservationCommand command = commandAssembler.toCommand(resource);
        Reservation saved = commandService.handle(command);
        ReservationResource resp = resourceAssembler.toResource(saved);
        return ResponseEntity.created(URI.create("/api/v1/reservations/" + saved.getId())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResource> getReservationById(@PathVariable Long id) {
        Optional<Reservation> found = queryService.handle(new GetReservationByIdQuery(id));
        return found.map(res -> ResponseEntity.ok(resourceAssembler.toResource(res)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResource> cancelReservation(@PathVariable Long id, @RequestBody @Valid CancelReservationResource resource) {
        var command = cancelAssembler.toCommand(id, resource);
        var canceled = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(canceled));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResource> modifyReservation(@PathVariable Long id, @RequestBody @Valid ModifyReservationResource resource) {
        var command = modifyAssembler.toCommand(id, resource);
        var updated = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(updated));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ReservationResource> updateReservationStatus(@PathVariable Long id, @RequestBody @Valid UpdateReservationStatusResource resource) {
        var command = updateStatusAssembler.toCommand(id, resource);
        var updated = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(updated));
    }

    @PostMapping("/{id}/confirm-return")
    public ResponseEntity<ReservationResource> confirmReturn(@PathVariable Long id, @RequestBody @Valid ConfirmReturnResource resource) {
        var command = confirmReturnAssembler.toCommand(id, resource);
        var updated = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(updated));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ReservationResource>> listByRenter(
            @RequestParam @Positive(message = "Renter ID must be positive") Long renterId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByRenterQuery(renterId, status));
        return ResponseEntity.ok(toPagedResponse(results, page, size, resourceAssembler::toResource));
    }

    @GetMapping("/owner")
    public ResponseEntity<PagedResponse<ReservationResource>> listByOwner(
            @RequestParam @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerQuery(ownerId, status));
        return ResponseEntity.ok(toPagedResponse(results, page, size, resourceAssembler::toResource));
    }

    @GetMapping("/renter/{renterId}/history")
    public ResponseEntity<PagedResponse<ReservationResource>> getRenterHistory(
            @PathVariable @Positive(message = "Renter ID must be positive") Long renterId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationHistoryByRenterQuery(renterId));
        return ResponseEntity.ok(toPagedResponse(results, page, size, resourceAssembler::toResource));
    }

    @GetMapping("/owner/paged")
    public ResponseEntity<PagedResponse<ReservationResource>> listByOwnerPaged(
            @RequestParam @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerPagedQuery(ownerId, status, page, size));
        var content = results.getContent().stream().map(resourceAssembler::toResource).toList();
        return ResponseEntity.ok(new PagedResponse<>(content, results.getNumber(), results.getSize(), results.getTotalElements(), results.getTotalPages()));
    }

    private <T, R> PagedResponse<R> toPagedResponse(List<T> source, int page, int size, Function<T, R> mapper) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        long totalElements = source.size();
        int fromIndex = Math.min(safePage * safeSize, source.size());
        int toIndex = Math.min(fromIndex + safeSize, source.size());
        List<R> content = source.subList(fromIndex, toIndex).stream().map(mapper).toList();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        return new PagedResponse<>(content, safePage, safeSize, totalElements, totalPages);
    }
}
