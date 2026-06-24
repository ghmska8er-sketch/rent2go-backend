package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.ReservationCommandService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.ModifyReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityQueryService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UpdateReservationStatusCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.NotificationService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
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
    private final NotificationService notificationService;
    private final VehicleRepository vehicleRepository;

    @Override
    public Reservation handle(CreateReservationCommand command) {
        // RES-01: resolve ownerId from vehicle instead of requiring it in the payload
        var vehicle = vehicleRepository.findById(command.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + command.vehicleId()));
        Long ownerId = vehicle.getOwnerId();

        DateRange requestedRange = DateRange.of(command.startDate(), command.endDate());

        // RES-02: validate no overlapping confirmed/active/pending reservation exists for this vehicle
        var existingReservations = reservationRepository.findAllByVehicleId(command.vehicleId());
        for (var existing : existingReservations) {
            var status = existing.getStatus().getStatus();
            if ("CONFIRMED".equals(status) || "ACTIVE".equals(status) || "PENDING".equals(status)) {
                if (existing.getDateRange().overlaps(requestedRange)) {
                    throw new IllegalStateException(
                        "El vehículo ya tiene una reserva para las fechas solicitadas.");
                }
            }
        }

        Reservation reservation = Reservation.create(
            command.vehicleId(),
            command.renterId(),
            ownerId,
            requestedRange,
            command.totalAmount()
        );

        // Apply optional pickup/return metadata if provided
        try {
            reservation.setPickupLocation(command.pickupLocation());
            reservation.setReturnLocation(command.returnLocation());
            reservation.setCoveragePlan(command.coveragePlan());
            reservation.setPickupPhotos(command.pickupPhotos());
            reservation.setReturnPhotos(command.returnPhotos());
        } catch (Exception ex) {
            // ignore metadata errors to avoid blocking reservation creation
        }

        var saved = reservationRepository.save(reservation);
        try {
            notificationService.notifyReservationCreated(saved.getId(), saved.getRenterId(), saved.getOwnerId());
        } catch (Exception ex) {
            // swallow notification errors; they must not break the booking flow
        }
        return saved;
    }

    @Override
    public Reservation handle(app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CancelReservationCommand command) {
        var reservationOpt = reservationRepository.findById(command.reservationId());
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found: " + command.reservationId());
        }

        var reservation = reservationOpt.get();
        reservation.cancel();
        var saved = reservationRepository.save(reservation);
        try {
            notificationService.notifyReservationCancelled(saved.getId(), saved.getRenterId(), saved.getOwnerId(), "Cancelled by renter");
        } catch (Exception ex) {
        }
        return saved;
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
        var previous = reservation.getStatus().getStatus();
        switch (t) {
            case "CONFIRMED" -> reservation.confirm();
            case "ACTIVE" -> reservation.activate();
            case "COMPLETED" -> reservation.complete();
            case "CANCELLED" -> reservation.cancel();
            default -> throw new IllegalArgumentException("Unsupported target status: " + target);
        }

        var saved = reservationRepository.save(reservation);
        try {
            notificationService.notifyReservationStatusChanged(saved.getId(), saved.getRenterId(), saved.getOwnerId(), previous, saved.getStatus().getStatus());
        } catch (Exception ex) {
        }

        return saved;
    }

    @Override
    public Reservation handle(app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.ConfirmReturnCommand command) {
        var reservationOpt = reservationRepository.findById(command.reservationId());
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found: " + command.reservationId());
        }

        var reservation = reservationOpt.get();

        // Only owner can confirm the return
        if (!reservation.getOwnerId().equals(command.actorId())) {
            throw new IllegalArgumentException("Actor is not authorized to confirm return");
        }

        if (!reservation.getStatus().isActive()) {
            throw new IllegalStateException("Only active reservations can be completed on return confirmation");
        }

        reservation.complete();
        var saved = reservationRepository.save(reservation);
        try {
            notificationService.notifyReservationStatusChanged(saved.getId(), saved.getRenterId(), saved.getOwnerId(), "ACTIVE", "COMPLETED");
        } catch (Exception ex) {
        }

        return saved;
    }
}