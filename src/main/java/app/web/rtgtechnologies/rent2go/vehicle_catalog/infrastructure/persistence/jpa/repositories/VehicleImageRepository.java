package app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VehicleImageRepository
 * 
 * Persistence interface for VehicleImage entity.
 * 
 * Hexagonal Architecture: Repository port for vehicle image data access.
 */
@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> {

    /**
     * Find all images for a specific vehicle.
     * 
     * @param vehicleId Vehicle ID
     * @return List of images for that vehicle
     */
    List<VehicleImage> findByVehicle_Id(Long vehicleId);

    /**
     * Find the primary image for a vehicle.
     * 
     * @param vehicleId Vehicle ID
     * @return Optional containing the primary image if it exists
     */
    @Query("SELECT vi FROM VehicleImage vi WHERE vi.vehicle.id = :vehicleId AND vi.isPrimary = true")
    Optional<VehicleImage> findPrimaryImageByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Count images for a vehicle.
     * 
     * @param vehicleId Vehicle ID
     * @return Number of images
     */
    long countByVehicle_Id(Long vehicleId);

    /**
     * Delete all images for a vehicle (cascade cleanup).
     * 
     * @param vehicleId Vehicle ID
     */
    void deleteByVehicle_Id(Long vehicleId);
}
