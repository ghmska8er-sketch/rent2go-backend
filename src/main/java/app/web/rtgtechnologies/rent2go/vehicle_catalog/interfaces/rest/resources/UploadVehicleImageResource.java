package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UploadVehicleImageResource
 *
 * Request DTO for uploading a vehicle image.
 * At least one of imagePath or imageUrl must be provided.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidImageSource
public class UploadVehicleImageResource {

    private String imagePath;

    @NotNull(message = "At least one of imagePath or imageUrl must be provided")
    private String imageUrl;

    private Boolean isPrimary;
    private Integer imageOrder;
}
