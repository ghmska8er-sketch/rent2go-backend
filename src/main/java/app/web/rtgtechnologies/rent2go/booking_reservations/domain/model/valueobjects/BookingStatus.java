package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * BookingStatus Value Object
 * 
 * Represents the state of a booking.
 * - PENDING: awaiting confirmation and payment
 * - CONFIRMED: payment received, awaiting pickup
 * - ACTIVE: customer has picked up vehicle
 * - RETURN_PENDING: vehicle is being returned
 * - RETURN_CONFIRMED: return has been acknowledged
 * - COMPLETED: vehicle returned, booking finished
 * - CANCELLED: booking cancelled by customer or system
 * - EXPIRED: booking expired before confirmation or pickup
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BookingStatus extends ValueObject {

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public static BookingStatus PENDING() {
        return new BookingStatus("PENDING");
    }

    public static BookingStatus CONFIRMED() {
        return new BookingStatus("CONFIRMED");
    }

    public static BookingStatus ACTIVE() {
        return new BookingStatus("ACTIVE");
    }

    public static BookingStatus RETURN_PENDING() {
        return new BookingStatus("RETURN_PENDING");
    }

    public static BookingStatus RETURN_CONFIRMED() {
        return new BookingStatus("RETURN_CONFIRMED");
    }

    public static BookingStatus COMPLETED() {
        return new BookingStatus("COMPLETED");
    }

    public static BookingStatus CANCELLED() {
        return new BookingStatus("CANCELLED");
    }

    public static BookingStatus EXPIRED() {
        return new BookingStatus("EXPIRED");
    }

    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(this.status);
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public boolean isReturnPending() {
        return "RETURN_PENDING".equals(this.status);
    }

    public boolean isReturnConfirmed() {
        return "RETURN_CONFIRMED".equals(this.status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(this.status);
    }

    public boolean isExpired() {
        return "EXPIRED".equals(this.status);
    }

    public boolean isTerminal() {
        return isCompleted() || isCancelled() || isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingStatus that = (BookingStatus) o;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}
