package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.BlockVehicleCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UnblockVehicleCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityCommandService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.VehicleAvailabilityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class VehicleAvailabilityCommandServiceImpl implements VehicleAvailabilityCommandService {

    private final VehicleAvailabilityRepository repository;

    @Override
    public VehicleAvailability handle(BlockVehicleCommand command) {
        DateRange range = DateRange.of(command.startDate(), command.endDate());
        List<VehicleAvailability> existing = repository.findAllByVehicleId(command.vehicleId());

        for (VehicleAvailability b : existing) {
            if (b.overlaps(range)) {
                throw new IllegalStateException("Requested block overlaps existing blocked range");
            }
        }

        VehicleAvailability block = VehicleAvailability.block(command.vehicleId(), range);
        return repository.save(block);
    }

    @Override
    public void handle(UnblockVehicleCommand command) {
        repository.deleteById(command.blockId());
    }
}
