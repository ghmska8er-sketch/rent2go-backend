package app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.specifications;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleCategory;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.SearchCriteria;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * VehicleSpecifications
 *
 * Sprint 5 (TS22, BRD-2026-07-05-Paginacion-Real-Backend-Vehiculos.md): composes every
 * DB-expressible vehicle search predicate into a single {@link Specification}, for use with
 * {@code VehicleRepository}'s already-declared-but-previously-unused {@code JpaSpecificationExecutor}
 * and a real {@code Pageable} request — replacing the prior "load all, filter in Java, slice
 * in the controller" approach.
 *
 * Per the BRD's recommended composition strategy (§7.2, R-001/R-003):
 * - price/category/seats/transmission/fuelType/location/status/feature-name are all pushed
 *   into this single DB-level query, alongside the {@code Pageable} request, so
 *   totalElements/totalPages are computed by the database's own COUNT query against the
 *   fully-filtered predicate — never a post-hoc in-memory count.
 * - The TS20 cross-context availability exclusion (blocked vehicle IDs from
 *   booking_reservations) is injected as a DB-level {@code NOT IN} predicate into this same
 *   query, per the BRD's Option (a): this preserves both pagination correctness AND the
 *   bounded-context anti-join mandate from BRD-2026-07-03-Disponibilidad-Avanzada-Busqueda-Vehiculos.md
 *   — the ID set itself is still resolved via a separate bulk lookup in
 *   {@code VehicleAvailabilityQueryService}, never a direct JPQL/native join across contexts.
 * - Geo-radius (Haversine) is deliberately NOT included here — per the BRD's explicit,
 *   documented exception (§7.2, §11 Open Question 1), it remains a narrowly-scoped,
 *   post-DB-query in-memory filter (see {@code VehicleQueryServiceImpl}), since no native DB
 *   geospatial function was introduced (consistent with Sprint 4's original decision).
 */
public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> fromCriteria(SearchCriteria criteria, VehicleStatus status, Set<Long> excludedVehicleIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), status));

            if (criteria.hasMinPrice()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dailyPrice"), criteria.getMinPrice()));
            }
            if (criteria.hasMaxPrice()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dailyPrice"), criteria.getMaxPrice()));
            }

            if (criteria.hasCategories()) {
                // Accepts category names or numeric IDs as strings, mirroring the prior
                // in-memory filter's exact semantics (VehicleQueryServiceImpl's matchesPrice-
                // adjacent category loop): match either the category's ID or its name.
                List<Predicate> categoryPredicates = new ArrayList<>();
                var categoryJoin = root.<Vehicle, VehicleCategory>join("category");
                for (String cat : criteria.getCategories()) {
                    try {
                        Long categoryId = Long.parseLong(cat);
                        categoryPredicates.add(cb.equal(categoryJoin.get("id"), categoryId));
                    } catch (NumberFormatException ignored) {
                        categoryPredicates.add(cb.equal(cb.lower(categoryJoin.get("name")), cat.toLowerCase()));
                    }
                }
                if (!categoryPredicates.isEmpty()) {
                    predicates.add(cb.or(categoryPredicates.toArray(new Predicate[0])));
                }
            }

            if (criteria.hasLocation()) {
                predicates.add(cb.equal(cb.lower(root.get("location")), criteria.getLocation().toLowerCase()));
            }

            if (criteria.hasYearRange()) {
                if (criteria.getMinYear() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("year"), criteria.getMinYear()));
                }
                if (criteria.getMaxYear() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("year"), criteria.getMaxYear()));
                }
            }

            if (criteria.hasSeats()) {
                predicates.add(cb.equal(root.get("seats"), criteria.getSeats()));
            }

            if (criteria.hasTransmission()) {
                predicates.add(cb.equal(cb.lower(root.get("transmission")), criteria.getTransmission().toLowerCase()));
            }

            if (criteria.hasFuelType()) {
                predicates.add(cb.equal(cb.lower(root.get("fuelType")), criteria.getFuelType().toLowerCase()));
            }

            if (criteria.getFeatureName() != null && !criteria.getFeatureName().isBlank()) {
                var featureJoin = root.join("features");
                predicates.add(cb.like(cb.lower(featureJoin.get("name")), "%" + criteria.getFeatureName().toLowerCase() + "%"));
                query.distinct(true);
            }

            // TS20 availability exclusion, injected as a DB-level NOT IN predicate (BRD's
            // Option (a)) — the ID set itself was already resolved via a separate,
            // bounded-context-respecting bulk lookup before this Specification is built.
            if (excludedVehicleIds != null && !excludedVehicleIds.isEmpty()) {
                predicates.add(cb.not(root.get("id").in(excludedVehicleIds)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
