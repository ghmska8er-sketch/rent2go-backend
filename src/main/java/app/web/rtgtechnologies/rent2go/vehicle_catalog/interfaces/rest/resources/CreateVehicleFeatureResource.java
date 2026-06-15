package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateVehicleFeatureResource
 *
 * Request DTO for creating a new VehicleFeature.
 *
 * Hexagonal Architecture: Request resource (interfaces layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleFeatureResource {

    @NotBlank(message = "Feature name is required")
    private String name;

    private String description;

    private String iconUrl;
}
