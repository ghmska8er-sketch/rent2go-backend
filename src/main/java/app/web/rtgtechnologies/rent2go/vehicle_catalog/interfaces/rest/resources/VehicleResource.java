package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * VehicleResource
 *
 * Response DTO for vehicle data.
 * Carries data from application layer back to HTTP layer.
 *
 * Hexagonal Architecture: Response resource (interfaces layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResource {

    private Long id;
    private Long ownerId;
    private String licensePlate;
    private String make;
    private String model;
    private Integer year;
    private String vin;
    private String status;
    private BigDecimal dailyPrice;
    private String categoryName;
    private String location;
    private String description;
    private Integer seats;
    private String transmission;
    private String fuelType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> features;
    private String primaryImageUrl;
    private String primaryImagePath;
    private Date createdAt;
    private Date updatedAt;
}
