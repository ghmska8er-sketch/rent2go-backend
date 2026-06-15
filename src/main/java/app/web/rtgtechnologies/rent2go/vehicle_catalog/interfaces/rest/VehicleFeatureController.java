package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleFeature;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.CreateVehicleFeatureResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleFeatureResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.VehicleFeatureResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleFeatureRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VehicleFeatureController
 *
 * REST endpoints for VehicleFeature aggregate root management.
 * Provides CRUD operations so the frontend can list available features,
 * search by name (autocomplete), and create new features on demand.
 *
 * Hexagonal Architecture: REST adapter (interface) for VehicleFeature.
 *
 * Base path: /api/v1/features
 */
@RestController
@RequestMapping("/api/v1/features")
@AllArgsConstructor
@Validated
@Tag(name = "Vehicle Features", description = "CRUD operations for vehicle features/amenities")
public class VehicleFeatureController {

    private final VehicleFeatureRepository vehicleFeatureRepository;

    /**
     * GET /api/v1/features
     *
     * List all available vehicle features.
     * Public endpoint — no authentication required.
     */
    @GetMapping
    @Operation(summary = "List all vehicle features")
    public ResponseEntity<List<VehicleFeatureResource>> listFeatures() {
        List<VehicleFeature> features = vehicleFeatureRepository.findAll();
        List<VehicleFeatureResource> resources = features.stream()
                .map(VehicleFeatureResourceFromEntityAssembler::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/features/{id}
     *
     * Get a vehicle feature by ID.
     * Public endpoint — no authentication required.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a vehicle feature by ID")
    public ResponseEntity<VehicleFeatureResource> getFeatureById(
            @PathVariable Long id) {
        VehicleFeature feature = vehicleFeatureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + id));
        VehicleFeatureResource resource = VehicleFeatureResourceFromEntityAssembler.toResource(feature);
        return ResponseEntity.ok(resource);
    }

    /**
     * GET /api/v1/features/name/{name}
     *
     * Get a vehicle feature by name (for autocomplete/search).
     * Public endpoint — no authentication required.
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "Get a vehicle feature by name")
    public ResponseEntity<VehicleFeatureResource> getFeatureByName(
            @PathVariable @NotBlank String name) {
        VehicleFeature feature = vehicleFeatureRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + name));
        VehicleFeatureResource resource = VehicleFeatureResourceFromEntityAssembler.toResource(feature);
        return ResponseEntity.ok(resource);
    }

    /**
     * POST /api/v1/features
     *
     * Create a new vehicle feature.
     * If a feature with the same name already exists, returns 409 Conflict.
     */
    @PostMapping
    @Operation(summary = "Create a new vehicle feature")
    public ResponseEntity<VehicleFeatureResource> createFeature(
            @RequestBody @Valid CreateVehicleFeatureResource resource) {
        // Check if feature with same name already exists
        if (vehicleFeatureRepository.findByName(resource.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        VehicleFeature feature = VehicleFeature.builder()
                .name(resource.getName())
                .description(resource.getDescription())
                .iconUrl(resource.getIconUrl())
                .build();

        VehicleFeature saved = vehicleFeatureRepository.save(feature);
        VehicleFeatureResource response = VehicleFeatureResourceFromEntityAssembler.toResource(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/features/{id}
     *
     * Update an existing vehicle feature.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a vehicle feature")
    public ResponseEntity<VehicleFeatureResource> updateFeature(
            @PathVariable Long id,
            @RequestBody @Valid CreateVehicleFeatureResource resource) {
        VehicleFeature feature = vehicleFeatureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + id));

        // Check if another feature already has the same name
        vehicleFeatureRepository.findByName(resource.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Feature name already exists: " + resource.getName());
            }
        });

        feature.update(resource.getName(), resource.getDescription(), resource.getIconUrl());

        VehicleFeature updated = vehicleFeatureRepository.save(feature);
        VehicleFeatureResource response = VehicleFeatureResourceFromEntityAssembler.toResource(updated);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/features/{id}
     *
     * Delete a vehicle feature.
     * Returns 409 if the feature is currently linked to any vehicle.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle feature")
    public ResponseEntity<Void> deleteFeature(@PathVariable Long id) {
        if (!vehicleFeatureRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        vehicleFeatureRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
