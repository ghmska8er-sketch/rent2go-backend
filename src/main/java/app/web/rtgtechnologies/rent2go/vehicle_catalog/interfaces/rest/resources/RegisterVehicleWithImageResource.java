package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * RegisterVehicleWithImageResource
 *
 * Request DTO for creating a vehicle with a primary image file upload.
 * Used by the POST /vehicles/with-image endpoint with multipart/form-data.
 *
 * Hexagonal Architecture: Request resource (interfaces layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterVehicleWithImageResource {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotBlank(message = "VIN is required")
    private String vin;

    @NotNull(message = "Daily price is required")
    private BigDecimal dailyPrice;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;

    private Integer seats;

    @NotBlank(message = "Transmission type is required")
    private String transmission;

    @NotBlank(message = "Fuel type is required")
    private String fuelType;

    private Double latitude;

    private Double longitude;

    private List<String> featureNames;
}
