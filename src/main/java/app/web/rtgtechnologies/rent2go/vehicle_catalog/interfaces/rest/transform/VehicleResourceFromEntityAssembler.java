package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleResource;

/**
 * VehicleResourceFromEntityAssembler
 *
 * Transforms Vehicle (Domain Aggregate) into VehicleResource (HTTP DTO).
 * Encapsulates the conversion logic between domain and interfaces layers.
 *
 * Hexagonal Architecture: Assembler (interfaces layer)
 */
public final class VehicleResourceFromEntityAssembler {

    /**
     * Convert domain aggregate to HTTP resource
     *
     * @param entity Vehicle aggregate from domain layer
     * @return VehicleResource for HTTP layer
     */
    public static VehicleResource toResource(Vehicle entity) {
        return VehicleResource.builder()
            .id(entity.getId())
            .licensePlate(entity.getLicensePlate())
            .make(entity.getMake())
            .model(entity.getModel())
            .year(entity.getYear())
            .vin(entity.getVin())
            .status(entity.getStatus().toString())
            .dailyPrice(entity.getDailyPrice())
            .categoryName(entity.getCategory().getName())
            .location(entity.getLocation())
            .description(entity.getDescription())
            .seats(entity.getSeats())
            .transmission(entity.getTransmission())
            .fuelType(entity.getFuelType())
            .primaryImageUrl(entity.getPrimaryImageUrl())
            .primaryImagePath(entity.getPrimaryImagePath())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
