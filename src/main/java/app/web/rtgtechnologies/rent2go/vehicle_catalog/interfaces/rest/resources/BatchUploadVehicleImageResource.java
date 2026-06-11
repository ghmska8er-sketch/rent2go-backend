package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * BatchUploadVehicleImageResource
 *
 * Request DTO for batch uploading vehicle images.
 * Only accepts an array of image URLs — no isPrimary or imageOrder metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUploadVehicleImageResource {

    @NotEmpty(message = "Image URLs array must not be empty")
    @NotNull(message = "Image URLs array must not be null")
    private List<String> imageUrls;
}
