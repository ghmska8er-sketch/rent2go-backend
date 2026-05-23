package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.BookingStatus;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.ReservationCode;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reservation Aggregate Root
 *
 * Represents the booking lifecycle for a rented vehicle.
 */
@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor
public class Reservation extends AuditableAbstractAggregateRoot<Reservation> {

    @Embedded
    private ReservationCode reservationCode;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false)),
        @AttributeOverride(name = "endDate", column = @Column(name = "end_date", nullable = false))
    })
    private DateRange dateRange;

    @Embedded
    private BookingStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Builder
    private Reservation(
        ReservationCode reservationCode,
        Long vehicleId,
        Long renterId,
        Long ownerId,
        DateRange dateRange,
        BookingStatus status,
        BigDecimal totalAmount,
        LocalDateTime cancelledAt
    ) {
        this.reservationCode = reservationCode;
        this.vehicleId = vehicleId;
        this.renterId = renterId;
        this.ownerId = ownerId;
        this.dateRange = dateRange;
        this.status = status;
        this.totalAmount = totalAmount;
        this.cancelledAt = cancelledAt;
    }

    public static Reservation create(
        Long vehicleId,
        Long renterId,
        Long ownerId,
        DateRange dateRange,
        BigDecimal totalAmount
    ) {
        if (vehicleId == null || renterId == null || ownerId == null) {
            throw new IllegalArgumentException("Vehicle, renter and owner identifiers are required");
        }

        if (dateRange == null) {
            throw new IllegalArgumentException("Date range is required");
        }

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount must be non-negative");
        }

        return Reservation.builder()
            .reservationCode(ReservationCode.generate())
            .vehicleId(vehicleId)
            .renterId(renterId)
            .ownerId(ownerId)
            .dateRange(dateRange)
            .status(BookingStatus.PENDING())
            .totalAmount(totalAmount)
            .build();
    }

    public void confirm() {
        ensurePending();
        this.status = BookingStatus.CONFIRMED();
    }

    public void activate() {
        if (!this.status.isConfirmed()) {
            throw new IllegalStateException("Reservation must be confirmed before activation");
        }

        this.status = BookingStatus.ACTIVE();
    }

    public void complete() {
        if (!this.status.isActive()) {
            throw new IllegalStateException("Reservation must be active before completion");
        }

        this.status = BookingStatus.COMPLETED();
    }

    public void cancel() {
        if (this.status.isCompleted()) {
            throw new IllegalStateException("Completed reservations cannot be cancelled");
        }

        this.status = BookingStatus.CANCELLED();
        this.cancelledAt = LocalDateTime.now();
    }

    private void ensurePending() {
        if (!this.status.isPending()) {
            throw new IllegalStateException("Reservation must be pending before confirmation");
        }
    }
}