package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityQueryService;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.VehicleAvailabilityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class VehicleAvailabilityQueryServiceImpl implements VehicleAvailabilityQueryService {

    private final VehicleAvailabilityRepository repository;

    @Override
    public List<VehicleAvailability> findByVehicleId(Long vehicleId) {
        return repository.findAllByVehicleId(vehicleId);
    }

    @Override
    public boolean isAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        var range = app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange.of(startDate, endDate);
        var existing = repository.findAllByVehicleId(vehicleId);
        for (VehicleAvailability b : existing) {
            if (b.overlaps(range)) return false;
        }
        return true;
    }
}
