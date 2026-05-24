package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

/**
 * ReservationCode Value Object
 * 
 * Unique identifier for a booking/reservation.
 * Generated automatically using UUID.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReservationCode extends ValueObject {

    @Column(name = "reservation_code", nullable = false, unique = true, length = 20)
    private String code;

    public static ReservationCode generate() {
        return new ReservationCode("RENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationCode that = (ReservationCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
