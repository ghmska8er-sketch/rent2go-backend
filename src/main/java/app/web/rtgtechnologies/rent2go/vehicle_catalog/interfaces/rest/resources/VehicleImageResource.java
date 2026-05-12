package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * VehicleImageResource
 *
 * Response DTO for vehicle image data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImageResource {

    private Long id;
    private String imagePath;
    private String imageUrl;
    private Boolean isPrimary;
    private LocalDateTime uploadDate;
}
