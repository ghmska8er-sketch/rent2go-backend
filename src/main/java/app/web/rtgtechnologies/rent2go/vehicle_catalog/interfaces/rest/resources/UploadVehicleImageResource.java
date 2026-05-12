package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UploadVehicleImageResource
 *
 * Request DTO for uploading a vehicle image.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadVehicleImageResource {

    @NotBlank(message = "Image path is required")
    private String imagePath;

    private String imageUrl;

    private Boolean isPrimary;
}
