package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.ReservationCommandService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
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
}