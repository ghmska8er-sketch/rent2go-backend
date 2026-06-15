package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RegisterVehicleWithImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleDetailsQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleImagesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehiclesByOwnerQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleCommandService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.SearchCriteria;
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
     * The image file is uploaded to Cloudinary and the returned URL is set as the primary image.
     * This endpoint accepts multipart/form-data with a vehicle JSON body and an image file.
     */
    @PostMapping(path = "/with-image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Publish a new vehicle with a primary image file upload")
    public ResponseEntity<VehicleResource> registerVehicleWithImageFile(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestPart("vehicle") String vehicleJson,
        @RequestPart("file") org.springframework.web.multipart.MultipartFile file
    ) throws java.io.IOException {

        Long ownerId = extractUserIdFromAuthHeader(authHeader);

        // Parse JSON to resource manually to avoid Content-Type issues
        ObjectMapper mapper = new ObjectMapper();
        RegisterVehicleWithImageResource vehicleResource = mapper.readValue(vehicleJson, RegisterVehicleWithImageResource.class);

        // Upload image to Cloudinary
        String imageUrl = cloudinaryStorageService.upload(file);

        // Convert resource to command using assembler
        var command = RegisterVehicleWithImageCommandFromResourceAssembler.toCommand(ownerId, vehicleResource, imageUrl);

        // Execute command
        Vehicle vehicle = vehicleCommandService.handle(command);

        // Convert entity to resource using assembler
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

        List<Vehicle> vehicles = vehicleQueryService.handle(new GetVehiclesByOwnerQuery(ownerId));
        return ResponseEntity.ok(toPagedResponse(vehicles, page, size, VehicleResourceFromEntityAssembler::toResource));
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
            featureName
        );

        List<Vehicle> vehicles = vehicleQueryService.handle(new SearchVehiclesByCriteriaQuery(criteria));
        return ResponseEntity.ok(toPagedResponse(vehicles, page, size, VehicleResourceFromEntityAssembler::toResource));
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
        UploadVehicleImageResource resource = new UploadVehicleImageResource(null, imageUrl, isPrimary, imageOrder);
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

    private Long extractUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }

        String token = authHeader.substring(7);
        return jwtTokenProvider.extractUserIdFromToken(token);
    }

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
