package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityQueryService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.VehicleAvailabilityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class VehicleAvailabilityQueryServiceImpl implements VehicleAvailabilityQueryService {

    private final VehicleAvailabilityRepository repository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<VehicleAvailability> findByVehicleId(Long vehicleId) {
        return repository.findAllByVehicleId(vehicleId);
    }

    @Override
    public boolean isAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        var range = DateRange.of(startDate, endDate);
        var existing = repository.findAllByVehicleId(vehicleId);
        for (VehicleAvailability b : existing) {
            if (b.overlaps(range)) return false;
        }
        return true;
    }

    @Override
    public Set<Long> findBlockedVehicleIds(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return Set.of();
        }
        var range = DateRange.of(startDate, endDate);
        Set<Long> blocked = new HashSet<>();

        for (VehicleAvailability block : repository.findAll()) {
            if (block.overlaps(range)) {
                blocked.add(block.getVehicleId());
            }
        }

        for (var reservation : reservationRepository.findAllInBlockingStatus()) {
            if (reservation.getDateRange().overlaps(range)) {
                blocked.add(reservation.getVehicleId());
            }
        }

        return blocked;
    }
}
