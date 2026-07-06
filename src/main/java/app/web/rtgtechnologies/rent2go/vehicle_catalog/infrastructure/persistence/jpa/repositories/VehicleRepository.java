package app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
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

    List<Vehicle> findByOwnerId(Long ownerId);

    /**
     * Sprint 5 (TS22): real DB-level paginated variant for GET /api/v1/vehicles/me, replacing
     * the prior load-all-then-slice-in-Java approach.
     *
     * Perf fix (2026-07-06): {@code @EntityGraph} eager-fetches {@code category} and
     * {@code features} in the same query. Without it, {@code VehicleResourceFromEntityAssembler}
     * triggers 2 additional lazy-load queries PER vehicle row when it reads
     * {@code entity.getCategory().getName()} (LAZY {@code @ManyToOne}) and
     * {@code entity.getFeatures()} (LAZY {@code @ManyToMany}) — for a 50-row page that is up to
     * 100 extra sequential round-trips, contributing to the reported slow response times
     * alongside the equivalent reservations-side N+1 fix.
     */
    @EntityGraph(attributePaths = {"category", "features"})
    Page<Vehicle> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Perf fix (2026-07-06): entity-graph-aware counterpart of the inherited
     * {@code JpaSpecificationExecutor.findAll(Specification, Pageable)}, used by
     * {@code VehicleQueryServiceImpl.handlePaged(SearchVehiclesByCriteriaQuery, ...)} (backing
     * GET /api/v1/vehicles) so {@code category} and {@code features} are eager-fetched in the
     * same query instead of triggering 2 lazy-load queries per row during response
     * serialization — same rationale as {@link #findByOwnerId(Long, Pageable)} above.
     * {@code @EntityGraph} cannot be attached directly to the inherited
     * {@code JpaSpecificationExecutor} method, hence this explicit override with an identical
     * signature/behavior otherwise.
     */
    @EntityGraph(attributePaths = {"category", "features"})
    Page<Vehicle> findAll(Specification<Vehicle> spec, Pageable pageable);

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
