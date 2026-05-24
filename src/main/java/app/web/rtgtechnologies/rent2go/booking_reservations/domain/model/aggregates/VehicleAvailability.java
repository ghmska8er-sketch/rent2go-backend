package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "vehicle_availabilities")
@Getter
@NoArgsConstructor
public class VehicleAvailability extends AuditableAbstractAggregateRoot<VehicleAvailability> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false)),
        @AttributeOverride(name = "endDate", column = @Column(name = "end_date", nullable = false))
    })
    private DateRange dateRange;

    private VehicleAvailability(Long vehicleId, DateRange dateRange) {
        this.vehicleId = vehicleId;
        this.dateRange = dateRange;
    }

    public static VehicleAvailability block(Long vehicleId, DateRange dateRange) {
        if (vehicleId == null || dateRange == null) {
            throw new IllegalArgumentException("vehicleId and dateRange are required");
        }
        return new VehicleAvailability(vehicleId, dateRange);
    }

    public boolean overlaps(DateRange other) {
        return this.dateRange.overlaps(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleAvailability that = (VehicleAvailability) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
