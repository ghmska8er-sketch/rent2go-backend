package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * VehicleFeature Aggregate Root
 * 
 * Represents vehicle features/amenities (e.g., AC, GPS, insurance, etc.).
 * Vehicles can have multiple features.
 */
@Entity
@Table(name = "vehicle_features")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFeature extends AuditableAbstractAggregateRoot<VehicleFeature> {

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;
}
