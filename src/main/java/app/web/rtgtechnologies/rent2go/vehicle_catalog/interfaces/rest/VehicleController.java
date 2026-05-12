package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetAvailableVehiclesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleDetailsQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleImagesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleCommandService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.CreateVehicleResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UpdateVehicleDetailsResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UpdateVehiclePricingResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UploadVehicleImageResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleImageResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.UpdateVehicleDetailsCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.UpdateVehiclePricingCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.UploadVehicleImageCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.VehicleImageResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * VehicleController
 *
 * REST endpoints for vehicle-catalog bounded context.
 *
 * Hexagonal Architecture: REST adapter (interface) for the domain.
 * Exposes commands and queries as HTTP endpoints.
 *
 * Base path: /api/v1/vehicles
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@AllArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle Catalog Management API")
public class VehicleController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;

    /**
     * POST /api/v1/vehicles
     *
     * Register a new vehicle in the catalog.
     */
    @PostMapping
    @Operation(summary = "Register a new vehicle")
    public ResponseEntity<VehicleResource> registerVehicle(
        @RequestBody CreateVehicleResource request
    ) {
        // Convert resource to command using assembler
        var command = CreateVehicleCommandFromResourceAssembler.toCommand(request);

        // Execute command
        Vehicle vehicle = vehicleCommandService.handle(command);

        // Convert entity to resource using assembler
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/vehicles/{id}
     *
     * Retrieve details of a specific vehicle.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle details")
    public ResponseEntity<VehicleResource> getVehicleDetails(
        @PathVariable Long id
    ) {
        GetVehicleDetailsQuery query = new GetVehicleDetailsQuery(id);
        Vehicle vehicle = vehicleQueryService.handle(query);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/vehicles
     *
     * Search for available vehicles with optional filters.
     */
    @GetMapping
    @Operation(summary = "Search available vehicles")
    public ResponseEntity<List<VehicleResource>> searchAvailableVehicles(
        @RequestParam(required = false) List<String> categories,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) String location
    ) {
        GetAvailableVehiclesQuery query = new GetAvailableVehiclesQuery(
            categories,
            minPrice,
            maxPrice,
            location
        );

        List<Vehicle> vehicles = vehicleQueryService.handle(query);
        List<VehicleResource> response = vehicles.stream()
            .map(VehicleResourceFromEntityAssembler::toResource)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/vehicles/{id}/price
     *
     * Update the daily rental price of a vehicle.
     */
    @PutMapping("/{id}/price")
    @Operation(summary = "Update vehicle pricing")
    public ResponseEntity<VehicleResource> updateVehiclePrice(
        @PathVariable Long id,
        @RequestBody UpdateVehiclePricingResource request
    ) {
        // Convert resource to command using assembler
        var command = UpdateVehiclePricingCommandFromResourceAssembler.toCommand(request);

        // Execute command
        Vehicle vehicle = vehicleCommandService.handle(command);

        // Convert entity to resource using assembler
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/vehicles/{id}
     *
     * Update editable vehicle details.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle details")
    public ResponseEntity<VehicleResource> updateVehicleDetails(
        @PathVariable Long id,
        @RequestBody UpdateVehicleDetailsResource request
    ) {
        var command = UpdateVehicleDetailsCommandFromResourceAssembler.toCommand(id, request);
        Vehicle vehicle = vehicleCommandService.handle(command);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/vehicles/{id}/images
     *
     * Upload a single image for a vehicle.
     */
    @PostMapping("/{id}/images")
    @Operation(summary = "Upload a vehicle image")
    public ResponseEntity<VehicleResource> uploadVehicleImage(
        @PathVariable Long id,
        @RequestBody UploadVehicleImageResource request
    ) {
        var command = UploadVehicleImageCommandFromResourceAssembler.toCommand(id, request);
        Vehicle vehicle = vehicleCommandService.handle(command);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/vehicles/{id}/images/batch
     *
     * Upload multiple images for a vehicle in one request.
     */
    @PostMapping("/{id}/images/batch")
    @Transactional
    @Operation(summary = "Upload multiple vehicle images")
    public ResponseEntity<VehicleResource> uploadVehicleImagesBatch(
        @PathVariable Long id,
        @RequestBody List<UploadVehicleImageResource> requests
    ) {
        Vehicle vehicle = null;
        for (UploadVehicleImageResource request : requests) {
            var command = UploadVehicleImageCommandFromResourceAssembler.toCommand(id, request);
            vehicle = vehicleCommandService.handle(command);
        }

        if (vehicle == null) {
            vehicle = vehicleQueryService.handle(new GetVehicleDetailsQuery(id));
        }

        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/vehicles/{vehicleId}/images/{imageId}/primary
     *
     * Mark an image as the primary image for the vehicle.
     */
    @PutMapping("/{vehicleId}/images/{imageId}/primary")
    @Operation(summary = "Set primary vehicle image")
    public ResponseEntity<VehicleResource> setPrimaryImage(
        @PathVariable Long vehicleId,
        @PathVariable Long imageId
    ) {
        var command = new app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.SetPrimaryVehicleImageCommand(vehicleId, imageId);
        Vehicle vehicle = vehicleCommandService.handle(command);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/vehicles/{id}/images
     *
     * Retrieve all images for a vehicle.
     */
    @GetMapping("/{id}/images")
    @Operation(summary = "Get all vehicle images")
    public ResponseEntity<List<VehicleImageResource>> getVehicleImages(
        @PathVariable Long id
    ) {
        List<VehicleImage> images = vehicleQueryService.handle(new GetVehicleImagesQuery(id));
        List<VehicleImageResource> response = images.stream()
            .map(VehicleImageResourceFromEntityAssembler::toResource)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
