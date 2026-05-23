package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "favorites")
@Getter
@NoArgsConstructor
public class Favorite extends AuditableAbstractAggregateRoot<Favorite> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;


    private Favorite(Long renterId, Long vehicleId) {
        this.renterId = renterId;
        this.vehicleId = vehicleId;
    }

    public static Favorite of(Long renterId, Long vehicleId) {
        if (renterId == null || vehicleId == null) {
            throw new IllegalArgumentException("renterId and vehicleId are required");
        }
        return new Favorite(renterId, vehicleId);
    }
}
