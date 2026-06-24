package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest;

import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices.VehicleAvailabilityCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices.VehicleAvailabilityQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.BlockVehicleCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.BlockRequestResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Booking & Reservations", description = "Vehicle availability and calendar blocking operations")
@RestController
@RequestMapping("/api/v1/availability")
@AllArgsConstructor
public class AvailabilityController {

    private final VehicleAvailabilityCommandServiceImpl commandService;
    private final VehicleAvailabilityQueryServiceImpl queryService;
    private final BlockVehicleCommandFromResourceAssembler assembler;

    @PostMapping("/block")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Block vehicle availability", description = "Blocks a vehicle for a date range so it cannot be reserved.")
    public ResponseEntity<Void> blockVehicle(@RequestBody BlockRequestResource resource) {
        var cmd = assembler.toCommand(resource);
        var saved = commandService.handle(cmd);
        return ResponseEntity.created(URI.create("/api/v1/availability/" + saved.getId())).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Unblock by ID (legacy)", description = "Removes a specific availability block by its ID.")
    public ResponseEntity<Void> unblockVehicle(@PathVariable Long id, @RequestParam Long requestedBy) {
        commandService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UnblockVehicleCommand(id, requestedBy));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/vehicle/{vehicleId}/range")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Unblock vehicle by date range",
               description = "Removes or trims all availability blocks that overlap the specified date range. Blocks partially outside the range are shortened, not deleted entirely.")
    public ResponseEntity<Void> unblockByRange(
            @PathVariable Long vehicleId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        commandService.unblockByRange(vehicleId, startDate, endDate);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicle/{vehicleId}/blocks")
    @Operation(summary = "List vehicle availability blocks",
               description = "Returns all active date-range blocks for a vehicle so clients can display a calendar of unavailable periods.")
    public ResponseEntity<List<Map<String, Object>>> listBlocks(@PathVariable Long vehicleId) {
        var blocks = queryService.findByVehicleId(vehicleId);
        var result = blocks.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("startDate", b.getDateRange().getStartDate().toString());
            m.put("endDate", b.getDateRange().getEndDate().toString());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vehicle/{vehicleId}/check")
    @Operation(summary = "Check vehicle availability",
               description = "Returns availability status and all blocked date ranges for a vehicle.")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        var allBlocks = queryService.findByVehicleId(vehicleId);
        boolean available = true;
        if (startDate != null && endDate != null) {
            available = queryService.isAvailable(vehicleId, startDate, endDate);
        } else if (!allBlocks.isEmpty()) {
            available = false;
        }

        var blockedRanges = allBlocks.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("startDate", b.getDateRange().getStartDate().toString());
            m.put("endDate", b.getDateRange().getEndDate().toString());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("vehicleId", vehicleId);
        response.put("isAvailable", available);
        response.put("blockedRanges", blockedRanges);
        return ResponseEntity.ok(response);
    }
}
