package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries;

/**
 * GetVehiclesByOwnerQuery
 *
 * Query for retrieving all vehicles published by a specific owner.
 */
public record GetVehiclesByOwnerQuery(Long ownerId) {
}