package app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ReservationRepository
 *
 * Persistence interface for the Reservation aggregate root.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode_Code(String code);

    List<Reservation> findAllByRenterId(Long renterId);

    List<Reservation> findAllByOwnerId(Long ownerId);
    
    List<Reservation> findAllByRenterIdAndStatus_Status(Long renterId, String status);

    List<Reservation> findAllByOwnerIdAndStatus_Status(Long ownerId, String status);
    
    List<Reservation> findAllByVehicleId(Long vehicleId);

    List<Reservation> findAllByVehicleIdAndIdNot(Long vehicleId, Long id);
}