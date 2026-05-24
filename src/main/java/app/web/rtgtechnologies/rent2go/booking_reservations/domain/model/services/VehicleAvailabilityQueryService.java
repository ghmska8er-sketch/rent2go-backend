package app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;

import java.time.LocalDate;
import java.util.List;

public interface VehicleAvailabilityQueryService {

    List<VehicleAvailability> findByVehicleId(Long vehicleId);

    boolean isAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate);
}
