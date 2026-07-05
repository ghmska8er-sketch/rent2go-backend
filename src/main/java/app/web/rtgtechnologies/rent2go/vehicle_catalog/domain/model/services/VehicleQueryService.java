package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetAvailableVehiclesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleDetailsQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleImagesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehiclesByOwnerQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * VehicleQueryService
 *
 * Domain service interface for vehicle query handling.
 * Defines contract for query handlers in the application layer.
 *
 * Hexagonal Architecture: Service port (domain layer interface)
 */
public interface VehicleQueryService {

    /**
     * Get details of a specific vehicle by ID
     *
     * @param query GetVehicleDetailsQuery with vehicle ID
     * @return Vehicle aggregate with full details
     */
    Vehicle handle(GetVehicleDetailsQuery query);

    /**
     * Get all images for a specific vehicle
     *
     * @param query GetVehicleImagesQuery with vehicle ID
     * @return List of images for that vehicle
     */
    List<VehicleImage> handle(GetVehicleImagesQuery query);

    /**
     * Get all available vehicles matching criteria
     *
     * @param query GetAvailableVehiclesQuery with filter criteria
     * @return List of available vehicles
     */
    List<Vehicle> handle(GetAvailableVehiclesQuery query);

    /**
     * Get all vehicles published by a specific owner.
     *
     * @param query GetVehiclesByOwnerQuery with owner ID
     * @return List of vehicles owned by that user
     */
    List<Vehicle> handle(GetVehiclesByOwnerQuery query);

    /**
     * Search available vehicles by search criteria (HU02, HU03)
     *
     * @param query SearchVehiclesByCriteriaQuery with search filters
     * @return List of vehicles matching criteria
     */
    List<Vehicle> handle(SearchVehiclesByCriteriaQuery query);

    /**
     * Get all available vehicles (no filter)
     *
     * @return List of all available vehicles
     */
    List<Vehicle> getAllAvailableVehicles();

    /**
     * Sprint 5 (TS22, BRD-2026-07-05-Paginacion-Real-Backend-Vehiculos.md): real DB-level
     * paginated search, replacing the prior load-all-then-slice-in-Java approach. Additive —
     * the existing {@link #handle(SearchVehiclesByCriteriaQuery)} list-returning method is
     * unchanged for any other caller.
     *
     * @param query    SearchVehiclesByCriteriaQuery with search filters
     * @param pageable page/size/sort request, applied at the database level
     * @return a Page of vehicles matching criteria, with a database-computed total count
     */
    Page<Vehicle> handlePaged(SearchVehiclesByCriteriaQuery query, Pageable pageable);

    /**
     * Sprint 5 (TS22) — real DB-level paginated variant of
     * {@link #handle(GetVehiclesByOwnerQuery)}, for {@code GET /api/v1/vehicles/me}.
     *
     * @param query    GetVehiclesByOwnerQuery with owner ID
     * @param pageable page/size/sort request, applied at the database level
     * @return a Page of vehicles owned by that user, with a database-computed total count
     */
    Page<Vehicle> handlePaged(GetVehiclesByOwnerQuery query, Pageable pageable);
}
