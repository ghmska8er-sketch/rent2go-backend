package app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleAvailabilityRepository extends JpaRepository<VehicleAvailability, Long> {

    List<VehicleAvailability> findAllByVehicleId(Long vehicleId);
}
