package app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * PricingRepository
 * 
 * Persistence interface for Pricing aggregate root.
 */
@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {

    List<Pricing> findByVehicleId(Long vehicleId);

    List<Pricing> findByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Long vehicleId,
        Instant startDate,
        Instant endDate
    );
}
