package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Pricing Aggregate Root
 * 
 * Represents pricing rules for a vehicle during specific time periods.
 * Supports seasonal pricing and dynamic pricing strategies.
 */
@Entity
@Table(name = "vehicle_pricings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pricing extends AuditableAbstractAggregateRoot<Pricing> {

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Column(length = 100)
    private String pricingType;  // e.g., SEASONAL, PROMOTIONAL, STANDARD
}
