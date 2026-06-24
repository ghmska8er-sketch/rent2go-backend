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

    /**
     * RES-05: Remove all blocks for a vehicle that overlap the given date range.
     * Blocks partially outside the range are trimmed rather than fully deleted.
     */
    public void unblockByRange(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        DateRange removeRange = DateRange.of(startDate, endDate);
        List<VehicleAvailability> blocks = repository.findAllByVehicleId(vehicleId);

        for (VehicleAvailability block : blocks) {
            if (!block.overlaps(removeRange)) continue;

            LocalDate bStart = block.getDateRange().getStartDate();
            LocalDate bEnd = block.getDateRange().getEndDate();

            boolean trimLeft  = bStart.isBefore(startDate);
            boolean trimRight = bEnd.isAfter(endDate);

            if (trimLeft && trimRight) {
                // Block spans the entire remove range — split into two
                repository.delete(block);
                repository.save(VehicleAvailability.block(vehicleId, DateRange.of(bStart, startDate.minusDays(1))));
                repository.save(VehicleAvailability.block(vehicleId, DateRange.of(endDate.plusDays(1), bEnd)));
            } else if (trimLeft) {
                // Block starts before remove range — shorten end
                repository.delete(block);
                repository.save(VehicleAvailability.block(vehicleId, DateRange.of(bStart, startDate.minusDays(1))));
            } else if (trimRight) {
                // Block ends after remove range — shorten start
                repository.delete(block);
                repository.save(VehicleAvailability.block(vehicleId, DateRange.of(endDate.plusDays(1), bEnd)));
            } else {
                // Block fully inside remove range — delete entirely
                repository.delete(block);
            }
        }
    }
}
