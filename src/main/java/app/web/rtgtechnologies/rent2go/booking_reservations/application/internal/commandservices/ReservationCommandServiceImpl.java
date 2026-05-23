package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.ReservationCommandService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.ModifyReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityQueryService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UpdateReservationStatusCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.BookingStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReservationCommandServiceImpl
 *
 * Application service responsible for booking commands.
 */
@Service
@AllArgsConstructor
@Transactional
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final VehicleAvailabilityQueryService availabilityQueryService;

    @Override
    public Reservation handle(CreateReservationCommand command) {
        Reservation reservation = Reservation.create(
            command.vehicleId(),
            command.renterId(),
            command.ownerId(),
            DateRange.of(command.startDate(), command.endDate()),
            command.totalAmount()
        );

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation handle(app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CancelReservationCommand command) {
        var reservationOpt = reservationRepository.findById(command.reservationId());
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found: " + command.reservationId());
        }

        var reservation = reservationOpt.get();
        reservation.cancel();
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation handle(ModifyReservationCommand command) {
        var reservationOpt = reservationRepository.findById(command.reservationId());
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found: " + command.reservationId());
        }

        var reservation = reservationOpt.get();
        if (reservation.getStatus().isTerminal()) {
            throw new IllegalStateException("Cannot modify a terminal reservation");
        }

        var vehicleId = reservation.getVehicleId();
        var newRange = DateRange.of(command.startDate(), command.endDate());

        // Check vehicle availability blocks
        if (!availabilityQueryService.isAvailable(vehicleId, command.startDate(), command.endDate())) {
            throw new IllegalStateException("Vehicle is not available for the requested dates");
        }

        // Check overlapping reservations (excluding current)
        var others = reservationRepository.findAllByVehicleIdAndIdNot(vehicleId, reservation.getId());
        for (var other : others) {
            if (other.getDateRange().overlaps(newRange)) {
                throw new IllegalStateException("Requested dates overlap with another reservation");
            }
        }

        reservation.modify(newRange);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation handle(UpdateReservationStatusCommand command) {
        var reservationOpt = reservationRepository.findById(command.reservationId());
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found: " + command.reservationId());
        }

        var reservation = reservationOpt.get();

        // Authorization: only owner may change reservation lifecycle in this flow
        if (!reservation.getOwnerId().equals(command.actorId())) {
            throw new IllegalArgumentException("Actor is not authorized to change reservation status");
        }

        var target = command.status();
        if (target == null) {
            throw new IllegalArgumentException("Target status is required");
        }

        var t = target.toUpperCase().trim();
        switch (t) {
            case "CONFIRMED" -> reservation.confirm();
            case "ACTIVE" -> reservation.activate();
            case "COMPLETED" -> reservation.complete();
            case "CANCELLED" -> reservation.cancel();
            default -> throw new IllegalArgumentException("Unsupported target status: " + target);
        }

        return reservationRepository.save(reservation);
    }
}