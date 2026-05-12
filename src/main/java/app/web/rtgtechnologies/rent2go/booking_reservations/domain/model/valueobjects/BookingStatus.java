package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
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
 * - COMPLETED: vehicle returned, booking finished
 * - CANCELLED: booking cancelled by customer or system
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatus extends ValueObject {

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

    public static BookingStatus COMPLETED() {
        return new BookingStatus("COMPLETED");
    }

    public static BookingStatus CANCELLED() {
        return new BookingStatus("CANCELLED");
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
