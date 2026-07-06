package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleDetailsQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleImagesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehiclesByOwnerQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleCategory;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleCommandService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.SearchCriteria;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleCategoryRepository;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers.CounterpartyResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.CreateVehicleResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.RegisterVehicleWithImageResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UpdateVehicleDetailsResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UpdateVehiclePricingResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.UploadVehicleImageResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleImageResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.RegisterVehicleWithImageCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.UpdateVehicleDetailsCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.UpdateVehiclePricingCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.UploadVehicleImageCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.VehicleImageResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary.CloudinaryStorageService;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider;
import app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resource.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;

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
@Validated
@Tag(name = "Vehicle Catalog", description = "Operations for managing vehicles, pricing, details and images")
public class VehicleController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final CounterpartyResourceAssembler counterpartyResourceAssembler;

    /**
     * GET /api/v1/vehicles/categories
     *
     * List all available vehicle categories so clients know valid filter values.
     */
    @GetMapping("/categories")
    @Operation(summary = "List all vehicle categories")
    public ResponseEntity<List<java.util.Map<String, Object>>> getCategories() {
        var categories = vehicleCategoryRepository.findAll();
        var result = categories.stream()
            .map(c -> {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", c.getId());
                m.put("name", c.getName());
                m.put("description", c.getDescription());
                m.put("iconUrl", c.getIconUrl());
                return m;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/vehicles
     *
     * Register a new vehicle in the catalog.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Publish a new vehicle with specifications")
    public ResponseEntity<VehicleResource> registerVehicle(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestBody @Valid CreateVehicleResource request
    ) {
        Long ownerId = extractUserIdFromAuthHeader(authHeader);

        // Convert resource to command using assembler
        var command = CreateVehicleCommandFromResourceAssembler.toCommand(ownerId, request);

        // Execute command
        Vehicle vehicle = vehicleCommandService.handle(command);

        // Convert entity to resource using assembler
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/vehicles/with-image
     *
     * Register a new vehicle with a primary image file upload.
     * All vehicle fields are sent as regular form fields; the image is sent as a file part named "file".
     */
    @PostMapping(path = "/with-image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Publish a new vehicle with a primary image file upload",
               description = "Send all vehicle fields as multipart form fields plus the image as 'file'. No JSON part required.")
    public ResponseEntity<VehicleResource> registerVehicleWithImageFile(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam("licensePlate") String licensePlate,
        @RequestParam("make") String make,
        @RequestParam("model") String model,
        @RequestParam("year") Integer year,
        @RequestParam("vin") String vin,
        @RequestParam("dailyPrice") java.math.BigDecimal dailyPrice,
        @RequestParam("categoryId") Long categoryId,
        @RequestParam("location") String location,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "seats", required = false) Integer seats,
        @RequestParam("transmission") String transmission,
        @RequestParam("fuelType") String fuelType,
        @RequestParam(value = "latitude", required = false) Double latitude,
        @RequestParam(value = "longitude", required = false) Double longitude,
        @RequestParam(value = "featureNames", required = false) java.util.List<String> featureNames,
        @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) throws java.io.IOException {

        Long ownerId = extractUserIdFromAuthHeader(authHeader);

        String imageUrl = cloudinaryStorageService.upload(file);

        RegisterVehicleWithImageResource vehicleResource = new RegisterVehicleWithImageResource(
            licensePlate, make, model, year, vin, dailyPrice, categoryId, location,
            description, seats, transmission, fuelType, latitude, longitude, featureNames
        );

        var command = RegisterVehicleWithImageCommandFromResourceAssembler.toCommand(ownerId, vehicleResource, imageUrl);
        Vehicle vehicle = vehicleCommandService.handle(command);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/vehicles/me
     *
     * Retrieve vehicles published by the authenticated owner.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my published vehicles")
    public ResponseEntity<PagedResponse<VehicleResource>> getMyVehicles(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        Long ownerId = extractUserIdFromAuthHeader(authHeader);

        // TS22: real DB-level pagination, replacing the prior load-all-then-slice-in-Java
        // approach. toPageable() preserves this endpoint's pre-existing 1-indexed `page`
        // client contract exactly (page=1 was always treated as the first page here).
        Pageable pageable = toPageable(page, size);
        Page<Vehicle> vehiclePage = vehicleQueryService.handlePaged(new GetVehiclesByOwnerQuery(ownerId), pageable);
        return ResponseEntity.ok(toPagedResponse(vehiclePage, page, VehicleResourceFromEntityAssembler::toResource));
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
     * GET /api/v1/vehicles/{id}/owner-summary
     *
     * US76 (Sprint 5 fixes remaining scope) — closes the pre-booking gap: the renter viewing
     * a vehicle's detail screen before any reservation exists has no way to see the owner's
     * name/verification status, since {@code CounterpartyResource} was previously only ever
     * embedded post-booking inside {@code ReservationResource}/{@code ConversationResource}
     * (both of which require an existing reservation/conversation). This is the one new
     * public-safe read endpoint that fills that gap.
     *
     * Reuses the exact same {@link CounterpartyResourceAssembler} used by
     * {@code ReservationResourceFromEntityAssembler} and
     * {@code ConversationResourceFromEntityAssembler} — no KYC-join logic is duplicated here.
     * Returns only the fields {@link CounterpartyResource} already exposes elsewhere
     * (fullName, kycVerified, dniVerified, licenseVerified, profileImageUrl) — never email,
     * phone, or any other PII, consistent with the Sprint 4/5 BRDs' PII-scoping principle.
     *
     * Read-only, no authentication required (mirrors GET /api/v1/vehicles/{id}, which is also
     * public) — a prospective renter must be able to see this before any reservation exists.
     *
     * @return 200 with the owner's {@link CounterpartyResource} (fail-open fallback if the
     *         owner has no KycApplication/profile photo — never an exception), or 404 if the
     *         vehicle itself does not exist, following this controller's existing
     *         {@code existsById(...) -> notFound()} convention (no custom exception type is
     *         used anywhere in this codebase for 404s).
     */
    @GetMapping("/{id}/owner-summary")
    @Operation(summary = "Get a vehicle's owner identity/verification summary",
               description = "Public-safe read: returns the owner's name and verification badges (KYC/DNI/license) " +
                             "plus profile photo, reusing the same counterparty enrichment as reservation/conversation " +
                             "responses. Does not require an existing reservation. Returns 404 if the vehicle does not exist.")
    public ResponseEntity<CounterpartyResource> getVehicleOwnerSummary(@PathVariable Long id) {
        return vehicleRepository.findById(id)
            .map(vehicle -> ResponseEntity.ok(counterpartyResourceAssembler.toCounterparty(vehicle.getOwnerId())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/vehicles
     *
     * Search for available vehicles with optional filters.
     */
    @GetMapping
    @Operation(summary = "Search available vehicles")
    public ResponseEntity<PagedResponse<VehicleResource>> searchAvailableVehicles(
        @RequestParam(required = false) List<String> categories,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Integer minYear,
        @RequestParam(required = false) Integer maxYear,
        @RequestParam(required = false) Integer seats,
        @RequestParam(required = false) String transmission,
        @RequestParam(required = false) String fuelType,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) Double centerLatitude,
        @RequestParam(required = false) Double centerLongitude,
        @RequestParam(required = false) Double radiusKm,
        @RequestParam(required = false) String featureName,
        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        SearchCriteria criteria = SearchCriteria.full(
            categories,
            minPrice,
            maxPrice,
            minYear,
            maxYear,
            seats,
            transmission,
            fuelType,
            location,
            centerLatitude,
            centerLongitude,
            radiusKm,
            featureName,
            startDate,
            endDate
        );

        // TS22: real DB-level pagination via VehicleQueryServiceImpl.handlePaged, composing
        // ALL existing filters (price/category/seats/transmission/fuel/geo-radius) plus the
        // TS20 availability-exclusion NOT IN predicate into one query. See that method's
        // Javadoc for the one documented exception (geo-radius) and why it remains correct.
        Pageable pageable = toPageable(page, size);
        Page<Vehicle> vehiclePage = vehicleQueryService.handlePaged(new SearchVehiclesByCriteriaQuery(criteria), pageable);
        return ResponseEntity.ok(toPagedResponse(vehiclePage, page, VehicleResourceFromEntityAssembler::toResource));
    }

    /**
     * PUT /api/v1/vehicles/{id}/price
     *
     * Update the daily rental price of a vehicle.
     */
    @PutMapping("/{id}/price")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update vehicle pricing")
    public ResponseEntity<VehicleResource> updateVehiclePrice(
        @PathVariable Long id,
        @RequestBody @Valid UpdateVehiclePricingResource request
    ) {
        // Convert resource to command using assembler
        var command = UpdateVehiclePricingCommandFromResourceAssembler.toCommand(id, request);

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
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update vehicle details")
    public ResponseEntity<VehicleResource> updateVehicleDetails(
        @PathVariable Long id,
        @RequestBody @Valid UpdateVehicleDetailsResource request
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
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Upload a vehicle image")
    public ResponseEntity<VehicleResource> uploadVehicleImage(
        @PathVariable Long id,
        @RequestBody @Valid UploadVehicleImageResource request
    ) {
        var command = UploadVehicleImageCommandFromResourceAssembler.toCommand(id, request);
        Vehicle vehicle = vehicleCommandService.handle(command);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/vehicles/{id}/images/upload
     *
     * Upload a single image file for a vehicle. The file is uploaded to Cloudinary
     * and the returned URL is persisted via the existing domain command flow.
     */
    @PostMapping(path = "/{id}/images/upload", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Upload a vehicle image (multipart file)")
    public ResponseEntity<VehicleResource> uploadVehicleImageFile(
        @PathVariable Long id,
        @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
        @RequestParam(value = "isPrimary", required = false) Boolean isPrimary,
        @RequestParam(value = "imageOrder", required = false) Integer imageOrder
    ) throws java.io.IOException {

        String imageUrl = cloudinaryStorageService.upload(file);

        // Build resource and delegate to existing command flow
        UploadVehicleImageResource resource = new UploadVehicleImageResource("", imageUrl, isPrimary, imageOrder);
        var command = UploadVehicleImageCommandFromResourceAssembler.toCommand(id, resource);
        Vehicle vehicle = vehicleCommandService.handle(command);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/vehicles/{id}/images/upload/batch
     *
     * Upload multiple image files for a vehicle in a single multipart request.
     * The request should include multiple `files` parts.
     * No isPrimary or imageOrder parameters are accepted for batch uploads.
     */
    @PostMapping(path = "/{id}/images/upload/batch", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Upload multiple vehicle images (multipart batch)")
    public ResponseEntity<VehicleResource> uploadVehicleImagesBatchMultipart(
        @PathVariable Long id,
        @RequestParam("files") java.util.List<org.springframework.web.multipart.MultipartFile> files
    ) throws java.io.IOException {

        Vehicle vehicle = null;

        for (org.springframework.web.multipart.MultipartFile f : files) {
            String imageUrl = cloudinaryStorageService.upload(f);

            // imagePath as empty string, imageUrl from Cloudinary
            UploadVehicleImageResource resource = new UploadVehicleImageResource("", imageUrl, null, null);
            var command = UploadVehicleImageCommandFromResourceAssembler.toCommand(id, resource);
            vehicle = vehicleCommandService.handle(command);
        }

        if (vehicle == null) {
            vehicle = vehicleQueryService.handle(new GetVehicleDetailsQuery(id));
        }

        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/vehicles/{id}/images/batch
     *
     * Upload multiple images for a vehicle in one request.
     */
    @PostMapping("/{id}/images/batch")
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
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
    public ResponseEntity<PagedResponse<VehicleImageResource>> getVehicleImages(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        List<VehicleImage> images = vehicleQueryService.handle(new GetVehicleImagesQuery(id));
        return ResponseEntity.ok(toPagedResponse(images, page, size, VehicleImageResourceFromEntityAssembler::toResource));
    }

    /**
     * DELETE /api/v1/vehicles/{id}/images/{imageId}
     *
     * Remove an image from a vehicle.
     * If the removed image is the primary, the next image in order becomes primary.
     */
    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Remove an image from a vehicle")
    public ResponseEntity<VehicleResource> removeVehicleImage(
        @PathVariable Long id,
        @PathVariable Long imageId
    ) {
        var command = new app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RemoveVehicleImageCommand(id, imageId);
        Vehicle vehicle = vehicleCommandService.handle(command);
        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);
        return ResponseEntity.ok(response);
    }

    // VEH-04: owner can change status to AVAILABLE, MAINTENANCE, or INACTIVE/RETIRED
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update vehicle status",
               description = "Allows the vehicle owner to set status to AVAILABLE or MAINTENANCE. ADMIN can also set RETIRED.")
    public ResponseEntity<VehicleResource> updateVehicleStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));

        VehicleStatus newStatus;
        try {
            newStatus = VehicleStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        switch (newStatus) {
            case AVAILABLE -> vehicle.makeAvailable();
            case MAINTENANCE -> vehicle.markAsMaintenanceRequired();
            case RETIRED -> vehicle.retire();
            default -> { return ResponseEntity.badRequest().build(); }
        }

        vehicleRepository.save(vehicle);

        VehicleResource response = VehicleResourceFromEntityAssembler.toResource(vehicle);

        // US14 AC2: pausing (MAINTENANCE) a vehicle that has PENDING reservation requests
        // must surface a warning instead of silently succeeding with zero signal.
        if (newStatus == VehicleStatus.MAINTENANCE) {
            boolean hasPendingRequests = reservationRepository.findAllByVehicleId(id).stream()
                    .anyMatch(r -> "PENDING".equals(r.getStatus().getStatus()));
            if (hasPendingRequests) {
                response.setWarning("This vehicle has pending reservation requests. Pausing it will not automatically cancel them; review and resolve them separately.");
            }
        }

        return ResponseEntity.ok(response);
    }

    // VEH-02: ADMIN retires a vehicle (soft deactivation)
    @PostMapping("/{id}/retire")
    @Operation(summary = "Retire vehicle",
               description = "Marks a vehicle as RETIRED so it no longer appears in public search.")
    public ResponseEntity<VehicleResource> retireVehicle(@PathVariable Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
        vehicle.retire();
        vehicleRepository.save(vehicle);
        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResource(vehicle));
    }

    // VEH-02: ADMIN hard-deletes a vehicle only if it has no active reservations
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN ONLY] Delete vehicle",
               description = "Permanently deletes a vehicle. Returns 409 if the vehicle has PENDING, CONFIRMED, or ACTIVE reservations, " +
                             "with a message suggesting pausing/deactivating the vehicle instead.")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) return ResponseEntity.notFound().build();

        var reservations = reservationRepository.findAllByVehicleId(id);
        boolean hasActive = reservations.stream().anyMatch(r -> {
            String s = r.getStatus().getStatus();
            return "PENDING".equals(s) || "CONFIRMED".equals(s) || "ACTIVE".equals(s);
        });
        if (hasActive) {
            // US17 AC2: bare 409 is not enough — explicitly suggest pausing/deactivating
            // (despublicar) the vehicle as an alternative to deletion.
            java.util.Map<String, String> body = new java.util.LinkedHashMap<>();
            body.put("error", "Conflict");
            body.put("message", "This vehicle cannot be deleted because it has reservation history (PENDING, CONFIRMED, or ACTIVE reservations). "
                    + "Consider pausing it (set status to MAINTENANCE) or retiring/despublicando it instead of deleting it.");
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).body(body);
        }

        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }

        String token = authHeader.substring(7);
        return jwtTokenProvider.extractUserIdFromToken(token);
    }

    /**
     * TS22: converts this endpoint's pre-existing, client-visible 1-indexed `page` parameter
     * (page=1 is the first page, matching this controller's historical contract before this
     * migration) into a 0-indexed Spring Data {@link Pageable}, without changing that
     * client-visible contract.
     */
    private Pageable toPageable(int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        return PageRequest.of(safePage - 1, safeSize);
    }

    /**
     * TS22: rebuilds the exact same {@code PagedResponse} shape
     * (content/page/size/totalElements/totalPages) the prior in-memory-slicing
     * {@code toPagedResponse(List, int, int, Function)} produced, but now sourced directly
     * from a real {@link Page} whose totalElements/totalPages were computed by the database's
     * own COUNT query — no client-visible contract change.
     */
    private <T, R> PagedResponse<R> toPagedResponse(Page<T> source, int page, Function<T, R> mapper) {
        int safePage = Math.max(1, page);
        List<R> content = source.getContent().stream().map(mapper).collect(Collectors.toList());
        return new PagedResponse<>(content, safePage, source.getSize(), source.getTotalElements(), source.getTotalPages());
    }

    /**
     * Unchanged, in-memory slicing helper — out of TS22's scope (GET /{id}/images is a
     * small, per-vehicle, already-loaded collection, not the catalog-scale search/listing
     * endpoints this migration targets).
     */
    private <T, R> PagedResponse<R> toPagedResponse(List<T> source, int page, int size, Function<T, R> mapper) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        long totalElements = source.size();
        int fromIndex = Math.min((safePage - 1) * safeSize, source.size());
        int toIndex = Math.min(fromIndex + safeSize, source.size());
        List<R> content = source.subList(fromIndex, toIndex).stream().map(mapper).collect(Collectors.toList());
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        return new PagedResponse<>(content, safePage, safeSize, totalElements, totalPages);
    }
}
