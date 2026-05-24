package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest;

import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices.VehicleAvailabilityCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices.VehicleAvailabilityQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.BlockVehicleCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.BlockRequestResource;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/availability")
@AllArgsConstructor
public class AvailabilityController {

    private final VehicleAvailabilityCommandServiceImpl commandService;
    private final VehicleAvailabilityQueryServiceImpl queryService;
    private final BlockVehicleCommandFromResourceAssembler assembler;

    @PostMapping("/block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> blockVehicle(@RequestBody BlockRequestResource resource) {
        var cmd = assembler.toCommand(resource);
        var saved = commandService.handle(cmd);
        return ResponseEntity.created(URI.create("/api/v1/availability/" + saved.getId())).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> unblockVehicle(@PathVariable Long id, @RequestParam Long requestedBy) {
        commandService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UnblockVehicleCommand(id, requestedBy));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicle/{vehicleId}/check")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable Long vehicleId,
                                                     @RequestParam LocalDate startDate,
                                                     @RequestParam LocalDate endDate) {
        boolean available = queryService.isAvailable(vehicleId, startDate, endDate);
        return ResponseEntity.ok(available);
    }
}
