package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * RegisterVehicleWithImageResource
 *
 * Request DTO for creating a vehicle with a primary image URL.
 * Used by the POST /vehicles/with-image endpoint.
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

    private String primaryImageUrl;

    private Double latitude;

    private Double longitude;
}
