package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateVehicleDetailsResource
 *
 * Request DTO for editing a vehicle's profile details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleDetailsResource {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2099, message = "Year must be valid")
    private Integer year;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Vehicle must have at least 1 seat")
    private Integer seats;

    @NotBlank(message = "Transmission type is required")
    private String transmission;

    @NotBlank(message = "Fuel type is required")
    private String fuelType;
}
