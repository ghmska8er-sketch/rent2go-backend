package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * CreateVehicleResource
 *
 * Request DTO for vehicle registration.
 * Carries data from HTTP layer to the application layer.
 *
 * Hexagonal Architecture: Request resource (interfaces layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleResource {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2099, message = "Year must be valid")
    private Integer year;

    @NotBlank(message = "VIN is required")
    private String vin;

    @NotNull(message = "Daily price is required")
    @DecimalMin(value = "0.01", message = "Daily price must be greater than 0")
    private BigDecimal dailyPrice;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

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
