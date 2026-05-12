package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.SearchCriteria;

/**
 * SearchVehiclesByCriteriaQuery
 * 
 * Query to fetch vehicles matching specific search criteria.
 * Uses SearchCriteria value object to encapsulate filter parameters.
 * 
 * CQRS Pattern: Read-only query that does not modify state.
 */
public record SearchVehiclesByCriteriaQuery(
    SearchCriteria criteria
) {
}
