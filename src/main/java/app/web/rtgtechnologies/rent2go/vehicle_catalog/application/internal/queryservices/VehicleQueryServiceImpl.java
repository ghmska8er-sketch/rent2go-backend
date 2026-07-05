package app.web.rtgtechnologies.rent2go.vehicle_catalog.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetAvailableVehiclesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleDetailsQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleImagesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehiclesByOwnerQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.SearchCriteria;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.specifications.VehicleSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * VehicleQueryServiceImpl
 *
 * Query handler for vehicle searches and lookups.
 * Handles read-only queries (GetAvailableVehiclesQuery, GetVehicleDetailsQuery, SearchVehiclesByCriteriaQuery).
 *
 * Hexagonal Architecture: Query Handler (application layer)
 * CQRS Pattern: Query Handler
 *
 * Returns domain aggregates (not DTOs). DTOs are created by Assemblers in the interfaces layer.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;
    // Cross-bounded-context read dependency (vehicle_catalog -> booking_reservations), mirroring the
    // already-accepted community_trust -> iam precedent in ReviewCommandServiceImpl. Used only for
    // TS20's availability-aware search exclusion; batched (not per-vehicle) to avoid N+1 queries.
    private final VehicleAvailabilityQueryService vehicleAvailabilityQueryService;

    /**
     * Handle GetAvailableVehiclesQuery
     *
     * Retrieves available vehicles with optional filtering by price range.
     *
     * @param query GetAvailableVehiclesQuery with filter criteria
     * @return List of available Vehicle aggregates
     */
    @Override
    public List<Vehicle> handle(GetAvailableVehiclesQuery query) {
        if (query.minPrice() != null && query.maxPrice() != null) {
            return vehicleRepository.findAvailableVehiclesByPriceRange(
                VehicleStatus.AVAILABLE,
                BigDecimal.valueOf(query.minPrice()),
                BigDecimal.valueOf(query.maxPrice())
            );
        }

        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
    }

    /**
     * Handle GetVehiclesByOwnerQuery
     *
     * Retrieves vehicles published by a specific owner.
     *
     * @param query GetVehiclesByOwnerQuery with owner ID
     * @return List of Vehicle aggregates owned by the user
     */
    @Override
    public List<Vehicle> handle(GetVehiclesByOwnerQuery query) {
        return vehicleRepository.findByOwnerId(query.ownerId());
    }

    /**
     * Handle GetVehicleDetailsQuery
     *
     * Retrieves detailed information about a specific vehicle.
     *
     * @param query GetVehicleDetailsQuery with vehicle ID
     * @return Vehicle aggregate with full details
     * @throws IllegalArgumentException if vehicle not found
     */
    @Override
    public Vehicle handle(GetVehicleDetailsQuery query) {
        return vehicleRepository.findByIdWithImages(query.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Vehicle not found: " + query.vehicleId()
            ));
    }

    /**
     * Handle GetVehicleImagesQuery
     */
    @Override
    public List<VehicleImage> handle(GetVehicleImagesQuery query) {
        Vehicle vehicle = vehicleRepository.findByIdWithImages(query.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Vehicle not found: " + query.vehicleId()
            ));

        return vehicle.getImages();
    }

    /**
     * Handle SearchVehiclesByCriteriaQuery
     *
     * Searches for available vehicles matching the provided criteria.
     * Supports filtering by: price range, categories, and location.
     *
     * @param query SearchVehiclesByCriteriaQuery with search criteria
     * @return List of vehicles matching criteria
     */
    @Override
    public List<Vehicle> handle(SearchVehiclesByCriteriaQuery query) {
        SearchCriteria criteria = query.criteria();

        // Start with all available vehicles
        List<Vehicle> results = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);

        // Filter by price range if provided
        if (criteria.hasPrice()) {
            results = results.stream()
                .filter(v -> matchesPrice(v, criteria))
                .toList();
        }

        // Filter by categories if provided — accepts category names or numeric IDs as strings
        if (criteria.hasCategories()) {
            results = results.stream()
                .filter(v -> {
                    if (v.getCategory() == null) return false;
                    for (String cat : criteria.getCategories()) {
                        try {
                            Long categoryId = Long.parseLong(cat);
                            if (categoryId.equals(v.getCategory().getId())) return true;
                        } catch (NumberFormatException ignored) {
                            if (cat.equalsIgnoreCase(v.getCategory().getName())) return true;
                        }
                    }
                    return false;
                })
                .toList();
        }

        // Filter by location if provided
        if (criteria.hasLocation()) {
            results = results.stream()
                .filter(v -> v.getLocation() != null && 
                           v.getLocation().equalsIgnoreCase(criteria.getLocation()))
                .toList();
        }

        // Filter by year range if provided
        if (criteria.hasYearRange()) {
            results = results.stream()
                .filter(v -> matchesYear(v, criteria))
                .toList();
        }

        // Filter by seats if provided
        if (criteria.hasSeats()) {
            results = results.stream()
                .filter(v -> v.getSeats() != null && v.getSeats().equals(criteria.getSeats()))
                .toList();
        }

        // Filter by transmission if provided
        if (criteria.hasTransmission()) {
            results = results.stream()
                .filter(v -> v.getTransmission() != null && v.getTransmission().equalsIgnoreCase(criteria.getTransmission()))
                .toList();
        }

        // Filter by fuel type if provided
        if (criteria.hasFuelType()) {
            results = results.stream()
                .filter(v -> v.getFuelType() != null && v.getFuelType().equalsIgnoreCase(criteria.getFuelType()))
                .toList();
        }

        // Filter by geographic radius (Haversine formula) if provided
        if (criteria.hasRadius()) {
            final double centerLat = Math.toRadians(criteria.getCenterLatitude());
            final double centerLng = Math.toRadians(criteria.getCenterLongitude());
            final double radiusKm = criteria.getRadiusKm();

            results = results.stream()
                .filter(v -> {
                    BigDecimal vehicleLat = v.getLatitude();
                    BigDecimal vehicleLng = v.getLongitude();
                    if (vehicleLat == null || vehicleLng == null) {
                        return false;
                    }
                    double distance = calculateDistanceHaversine(centerLat, centerLng,
                        Math.toRadians(vehicleLat.doubleValue()), Math.toRadians(vehicleLng.doubleValue()));
                    return distance <= radiusKm;
                })
                .toList();
        }

        // Filter by feature name if provided
        if (criteria.getFeatureName() != null && !criteria.getFeatureName().isBlank()) {
            final String feature = criteria.getFeatureName().toLowerCase();
            results = results.stream()
                .filter(v -> v.getFeatures() != null &&
                           v.getFeatures().stream()
                               .anyMatch(f -> f.getName() != null && f.getName().toLowerCase().contains(feature)))
                .toList();
        }

        // TS20: exclude vehicles blocked for the requested date range (availability block or an
        // overlapping reservation in PENDING/CONFIRMED/ACTIVE/RETURN_PENDING/RETURN_CONFIRMED).
        // Delegated to booking_reservations' own batched query — no direct cross-context JPQL join,
        // consistent with the DDD bounded-context boundary respected elsewhere in this codebase.
        if (criteria.hasDateRange()) {
            var blockedVehicleIds = vehicleAvailabilityQueryService.findBlockedVehicleIds(
                criteria.getStartDate(), criteria.getEndDate());
            if (!blockedVehicleIds.isEmpty()) {
                results = results.stream()
                    .filter(v -> !blockedVehicleIds.contains(v.getId()))
                    .toList();
            }
        }

        return results;
    }

    /**
     * Sprint 5 (TS22, BRD-2026-07-05-Paginacion-Real-Backend-Vehiculos.md): real DB-level
     * paginated search. Price/category/year/seats/transmission/fuelType/location/feature-name
     * and the TS20 availability-exclusion NOT IN predicate are all composed into a single
     * {@link Specification}, so {@code Page.getTotalElements()}/{@code getTotalPages()} are
     * computed by the database's own COUNT query against the fully-filtered predicate.
     *
     * Geo-radius (Haversine) is the one deliberately-scoped exception per the BRD's explicit
     * recommendation (§7.2, §11 Open Question 1): it cannot be expressed as a JPA
     * {@code Specification} predicate without a native DB geospatial function, which Sprint 4
     * chose not to introduce. When a radius filter is present, this method evaluates every
     * DB-expressible predicate as an unpaged candidate set, applies the in-memory Haversine
     * filter to that full candidate set, and only then paginates in memory — preserving
     * correct, non-duplicated, non-missing results and an accurate totalElements/totalPages
     * for this specific, narrow, tested exception, at the cost of not being a DB-level
     * LIMIT/OFFSET for radius searches specifically.
     */
    @Override
    public Page<Vehicle> handlePaged(SearchVehiclesByCriteriaQuery query, Pageable pageable) {
        SearchCriteria criteria = query.criteria();

        Set<Long> excludedVehicleIds = Set.of();
        if (criteria.hasDateRange()) {
            excludedVehicleIds = vehicleAvailabilityQueryService.findBlockedVehicleIds(
                criteria.getStartDate(), criteria.getEndDate());
        }

        Specification<Vehicle> spec = VehicleSpecifications.fromCriteria(criteria, VehicleStatus.AVAILABLE, excludedVehicleIds);

        if (criteria.hasRadius()) {
            // Documented exception: DB-expressible filters resolved unpaged, Haversine and
            // pagination both applied in memory against that single, fully-filtered candidate
            // set -- never against an already-DB-paginated page (which would silently corrupt
            // totalElements/totalPages, see the BRD's R-001/R-003).
            List<Vehicle> candidates = vehicleRepository.findAll(spec);
            List<Vehicle> filtered = filterByRadius(candidates, criteria);
            return paginateInMemory(filtered, pageable);
        }

        return vehicleRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Vehicle> handlePaged(GetVehiclesByOwnerQuery query, Pageable pageable) {
        return vehicleRepository.findByOwnerId(query.ownerId(), pageable);
    }

    private List<Vehicle> filterByRadius(List<Vehicle> vehicles, SearchCriteria criteria) {
        final double centerLat = Math.toRadians(criteria.getCenterLatitude());
        final double centerLng = Math.toRadians(criteria.getCenterLongitude());
        final double radiusKm = criteria.getRadiusKm();

        return vehicles.stream()
            .filter(v -> {
                BigDecimal vehicleLat = v.getLatitude();
                BigDecimal vehicleLng = v.getLongitude();
                if (vehicleLat == null || vehicleLng == null) {
                    return false;
                }
                double distance = calculateDistanceHaversine(centerLat, centerLng,
                    Math.toRadians(vehicleLat.doubleValue()), Math.toRadians(vehicleLng.doubleValue()));
                return distance <= radiusKm;
            })
            .toList();
    }

    private Page<Vehicle> paginateInMemory(List<Vehicle> filtered, Pageable pageable) {
        int total = filtered.size();
        int fromIndex = Math.min((int) pageable.getOffset(), total);
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), total);
        List<Vehicle> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, pageable, total);
    }

    /**
     * Calculate distance between two points using the Haversine formula.
     * 
     * @param lat1 Latitude of point 1 in radians
     * @param lng1 Longitude of point 1 in radians
     * @param lat2 Latitude of point 2 in radians
     * @param lng2 Longitude of point 2 in radians
     * @return Distance in kilometers
     */
    private double calculateDistanceHaversine(double lat1, double lng1, double lat2, double lng2) {
        double earthRadiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    /**
     * Get all available vehicles without filters
     *
     * @return List of all available Vehicle aggregates
     */
    @Override
    public List<Vehicle> getAllAvailableVehicles() {
        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
    }

    private boolean matchesPrice(Vehicle vehicle, SearchCriteria criteria) {
        if (criteria.hasMinPrice() && vehicle.getDailyPrice().compareTo(criteria.getMinPrice()) < 0) {
            return false;
        }

        if (criteria.hasMaxPrice() && vehicle.getDailyPrice().compareTo(criteria.getMaxPrice()) > 0) {
            return false;
        }

        return true;
    }

    private boolean matchesYear(Vehicle vehicle, SearchCriteria criteria) {
        if (criteria.getMinYear() != null && vehicle.getYear() < criteria.getMinYear()) {
            return false;
        }

        if (criteria.getMaxYear() != null && vehicle.getYear() > criteria.getMaxYear()) {
            return false;
        }

        return true;
    }
}
