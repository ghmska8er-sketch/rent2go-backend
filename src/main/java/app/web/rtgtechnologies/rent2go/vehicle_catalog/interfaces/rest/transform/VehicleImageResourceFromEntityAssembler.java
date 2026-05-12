package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.transform;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources.VehicleImageResource;

/**
 * VehicleImageResourceFromEntityAssembler
 *
 * Transforms VehicleImage into VehicleImageResource.
 */
public final class VehicleImageResourceFromEntityAssembler {

    public static VehicleImageResource toResource(VehicleImage entity) {
        return VehicleImageResource.builder()
            .id(entity.getId())
            .imagePath(entity.getImagePath())
            .imageUrl(entity.getImageUrl())
            .isPrimary(entity.isPrimary())
            .uploadDate(entity.getUploadedDate())
            .build();
    }
}
