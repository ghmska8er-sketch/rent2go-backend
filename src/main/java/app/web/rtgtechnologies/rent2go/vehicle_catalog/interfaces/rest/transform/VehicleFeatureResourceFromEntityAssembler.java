package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleFeature;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleFeatureResource;

/**
 * VehicleFeatureResourceFromEntityAssembler
 *
 * Transforms VehicleFeature (Domain Aggregate) into VehicleFeatureResource (HTTP DTO).
 *
 * Hexagonal Architecture: Assembler (interfaces layer)
 */
public final class VehicleFeatureResourceFromEntityAssembler {

    /**
     * Convert domain aggregate to HTTP resource
     *
     * @param entity VehicleFeature aggregate from domain layer
     * @return VehicleFeatureResource for HTTP layer
     */
    public static VehicleFeatureResource toResource(VehicleFeature entity) {
        return VehicleFeatureResource.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .iconUrl(entity.getIconUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
