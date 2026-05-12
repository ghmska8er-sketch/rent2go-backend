package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries;

import java.util.List;

/**
 * GetAvailableVehiclesQuery
 * 
 * Query to fetch all available vehicles with optional filtering.
 * 
 * CQRS Pattern: Read-only query that does not modify state.
 */
public record GetAvailableVehiclesQuery(
    List<String> categories,
    Double minPrice,
    Double maxPrice,
    String location
) {
}
