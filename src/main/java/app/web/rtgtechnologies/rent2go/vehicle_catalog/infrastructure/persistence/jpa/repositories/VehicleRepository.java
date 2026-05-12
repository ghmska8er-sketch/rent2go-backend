package app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VehicleRepository
 * 
 * Persistence interface for Vehicle aggregate root.
 * Provides CRUD operations and custom queries for vehicle management.
 * 
 * Hexagonal Architecture: Repository port for vehicle data access.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    Optional<Vehicle> findByVin(String vin);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByCategoryId(Long categoryId);

    @Query("SELECT v FROM Vehicle v WHERE v.status = :status AND v.dailyPrice BETWEEN :minPrice AND :maxPrice")
    List<Vehicle> findAvailableVehiclesByPriceRange(
        @Param("status") VehicleStatus status,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice
    );

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status")
    long countVehiclesByStatus(@Param("status") VehicleStatus status);

    /**
     * Find vehicle by ID with all images eager-loaded.
     * 
     * @param id Vehicle ID
     * @return Optional containing Vehicle with images if found
     */
    @Query("SELECT DISTINCT v FROM Vehicle v LEFT JOIN FETCH v.images WHERE v.id = :id")
    Optional<Vehicle> findByIdWithImages(@Param("id") Long id);
}
