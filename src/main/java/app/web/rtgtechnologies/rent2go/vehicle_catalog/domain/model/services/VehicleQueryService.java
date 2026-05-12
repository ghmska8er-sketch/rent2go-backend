package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetAvailableVehiclesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleDetailsQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehicleImagesQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;

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
}
