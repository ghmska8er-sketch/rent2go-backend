package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * UpdateVehiclePricingResource
 *
 * Request DTO for updating vehicle pricing.
 * Carries data from HTTP layer to the application layer.
 *
 * Hexagonal Architecture: Request resource (interfaces layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehiclePricingResource {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "New daily price is required")
    @DecimalMin(value = "0.01", message = "Daily price must be greater than 0")
    private BigDecimal newDailyPrice;
}
