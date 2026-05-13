package app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * VehicleFeatureRepository
 * 
 * Persistence interface for VehicleFeature aggregate root.
 */
@Repository
public interface VehicleFeatureRepository extends JpaRepository<VehicleFeature, Long> {

    Optional<VehicleFeature> findByName(String name);
}
