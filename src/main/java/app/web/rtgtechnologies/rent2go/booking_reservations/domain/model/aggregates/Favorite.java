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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;


    private Favorite(Long userId, Long vehicleId) {
        this.userId = userId;
        this.vehicleId = vehicleId;
    }

    public static Favorite of(Long userId, Long vehicleId) {
        if (userId == null || vehicleId == null) {
            throw new IllegalArgumentException("userId and vehicleId are required");
        }
        return new Favorite(userId, vehicleId);
    }
}
