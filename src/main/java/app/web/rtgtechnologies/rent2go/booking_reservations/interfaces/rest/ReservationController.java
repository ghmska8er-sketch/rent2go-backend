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
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reservations")
@AllArgsConstructor
public class ReservationController {

    private final ReservationCommandServiceImpl commandService;
    private final ReservationQueryServiceImpl queryService;
    private final CreateReservationCommandFromResourceAssembler commandAssembler;
    private final ReservationResourceFromEntityAssembler resourceAssembler;
    private final CancelReservationCommandFromResourceAssembler cancelAssembler;
    private final ModifyReservationCommandFromResourceAssembler modifyAssembler;
    private final UpdateReservationStatusCommandFromResourceAssembler updateStatusAssembler;

    @PostMapping
    public ResponseEntity<ReservationResource> createReservation(@RequestBody CreateReservationResource resource) {
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
    public ResponseEntity<ReservationResource> cancelReservation(@PathVariable Long id, @RequestBody CancelReservationResource resource) {
        var command = cancelAssembler.toCommand(id, resource);
        var canceled = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(canceled));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResource> modifyReservation(@PathVariable Long id, @RequestBody ModifyReservationResource resource) {
        var command = modifyAssembler.toCommand(id, resource);
        var updated = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(updated));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ReservationResource> updateReservationStatus(@PathVariable Long id, @RequestBody UpdateReservationStatusResource resource) {
        var command = updateStatusAssembler.toCommand(id, resource);
        var updated = commandService.handle(command);
        return ResponseEntity.ok(resourceAssembler.toResource(updated));
    }

    @GetMapping
    public ResponseEntity<java.util.List<ReservationResource>> listByRenter(
            @RequestParam(required = false) Long renterId,
            @RequestParam(required = false) String status
    ) {
        if (renterId == null) {
            return ResponseEntity.badRequest().build();
        }

        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByRenterQuery(renterId, status));
        var payload = results.stream().map(resourceAssembler::toResource).toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/owner")
    public ResponseEntity<java.util.List<ReservationResource>> listByOwner(
            @RequestParam Long ownerId,
            @RequestParam(required = false) String status
    ) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerQuery(ownerId, status));
        var payload = results.stream().map(resourceAssembler::toResource).toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/renter/{renterId}/history")
    public ResponseEntity<java.util.List<ReservationResource>> getRenterHistory(@PathVariable Long renterId) {
        var results = queryService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationHistoryByRenterQuery(renterId));
        var payload = results.stream().map(resourceAssembler::toResource).toList();
        return ResponseEntity.ok(payload);
    }
}
