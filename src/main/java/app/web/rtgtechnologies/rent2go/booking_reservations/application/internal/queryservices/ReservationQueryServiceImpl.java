package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationByIdQuery;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerQuery;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByRenterQuery;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationHistoryByRenterQuery;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.queries.GetReservationsByOwnerPagedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.ReservationQueryService;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ReservationQueryServiceImpl
 *
 * Application service responsible for booking queries.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryServiceImpl implements ReservationQueryService {

    private final ReservationRepository reservationRepository;

    @Override
    public Optional<Reservation> handle(GetReservationByIdQuery query) {
        return reservationRepository.findById(query.reservationId());
    }

    @Override
    public java.util.List<Reservation> handle(GetReservationsByRenterQuery query) {
        if (query.status() == null || query.status().isBlank()) {
            return reservationRepository.findAllByRenterId(query.renterId());
        }

        return reservationRepository.findAllByRenterIdAndStatus_Status(query.renterId(), query.status());
    }

    @Override
    public java.util.List<Reservation> handle(GetReservationsByOwnerQuery query) {
        if (query.status() == null || query.status().isBlank()) {
            return reservationRepository.findAllByOwnerId(query.ownerId());
        }

        return reservationRepository.findAllByOwnerIdAndStatus_Status(query.ownerId(), query.status());
    }

    @Override
    public java.util.List<Reservation> handle(GetReservationHistoryByRenterQuery query) {
        // For now history is defined as completed reservations for the renter
        return reservationRepository.findAllByRenterIdAndStatus_Status(query.renterId(), "COMPLETED");
    }

    @Override
    public Page<Reservation> handle(GetReservationsByOwnerPagedQuery query) {
        var p = PageRequest.of(Math.max(1, query.page()) - 1, Math.max(1, query.size()));
        if (query.status() == null || query.status().isBlank()) {
            return reservationRepository.findAllByOwnerId(query.ownerId(), p);
        }

        return reservationRepository.findAllByOwnerIdAndStatus_Status(query.ownerId(), query.status(), p);
    }
}