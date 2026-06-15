package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * VehicleFeatureResource
 *
 * Response DTO for VehicleFeature data.
 *
 * Hexagonal Architecture: Response resource (interfaces layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFeatureResource {

    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private Date createdAt;
    private Date updatedAt;
}
