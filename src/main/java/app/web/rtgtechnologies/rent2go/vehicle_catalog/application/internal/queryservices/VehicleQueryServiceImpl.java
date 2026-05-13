package app.web.rtgtechnologies.rent2go.vehicle_catalog.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetAvailableVehiclesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleDetailsQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleImagesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.SearchCriteria;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
                .filter(v -> v.getDailyPrice().compareTo(criteria.getMinPrice()) >= 0 &&
                           v.getDailyPrice().compareTo(criteria.getMaxPrice()) <= 0)
                .toList();
        }

        // Filter by categories if provided
        if (criteria.hasCategories()) {
            results = results.stream()
                .filter(v -> criteria.getCategories().contains(v.getCategory().getName()))
                .toList();
        }

        // Filter by location if provided
        if (criteria.hasLocation()) {
            results = results.stream()
                .filter(v -> v.getLocation() != null && 
                           v.getLocation().equalsIgnoreCase(criteria.getLocation()))
                .toList();
        }

        return results;
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
}
