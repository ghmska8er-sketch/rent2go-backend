package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * VehicleCategory Aggregate Root
 * 
 * Represents vehicle categories (e.g., sedan, SUV, truck).
 * Allows filtering and searching vehicles by type.
 */
@Entity
@Table(name = "vehicle_categories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCategory extends AuditableAbstractAggregateRoot<VehicleCategory> {

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;
}
