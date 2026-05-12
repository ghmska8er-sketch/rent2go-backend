package app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * VehicleCategoryRepository
 * 
 * Persistence interface for VehicleCategory aggregate root.
 */
@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {

    Optional<VehicleCategory> findByName(String name);
}
