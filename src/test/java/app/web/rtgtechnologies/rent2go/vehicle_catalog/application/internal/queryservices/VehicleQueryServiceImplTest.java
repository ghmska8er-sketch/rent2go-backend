package app.web.rtgtechnologies.rent2go.vehicle_catalog.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehiclesByOwnerQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.SearchCriteria;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VehicleQueryServiceImplTest
 *
 * Sprint 5 (TS22, BRD-2026-07-05-Paginacion-Real-Backend-Vehiculos.md): regression coverage
 * for the migration from in-memory list-slicing to real DB-level Pageable/Specification
 * pagination. Covers: a no-filter request still delegates to a Pageable-aware repository
 * call (not a full-list load); the TS20 availability-exclusion ID set is resolved and
 * injected only when a date range is present; the geo-radius exception path evaluates
 * candidates unpaged then paginates in memory, producing accurate totalElements and correct,
 * non-duplicated page boundaries; and GetVehiclesByOwnerQuery delegates to the repository's
 * new Pageable-accepting findByOwnerId overload.
 */
@ExtendWith(MockitoExtension.class)
class VehicleQueryServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleAvailabilityQueryService vehicleAvailabilityQueryService;

    private VehicleQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VehicleQueryServiceImpl(vehicleRepository, vehicleAvailabilityQueryService);
    }

    private SearchCriteria emptyCriteria() {
        return SearchCriteria.full(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void handlePaged_withNoFilters_delegatesToRepositoryFindAllWithSpecificationAndPageable() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Vehicle> expected = new PageImpl<>(List.of(), pageable, 0);
        when(vehicleRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Vehicle>>any(), eq(pageable)))
                .thenReturn(expected);

        Page<Vehicle> result = service.handlePaged(new SearchVehiclesByCriteriaQuery(emptyCriteria()), pageable);

        assertEquals(expected, result);
        verify(vehicleAvailabilityQueryService, never()).findBlockedVehicleIds(any(), any());
    }

    @Test
    void handlePaged_withDateRange_resolvesBlockedIdsAndComposesIntoSpecification() {
        SearchCriteria criteria = SearchCriteria.full(null, null, null, null, null, null, null, null, null,
                null, null, null, null, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));
        Pageable pageable = PageRequest.of(0, 20);
        when(vehicleAvailabilityQueryService.findBlockedVehicleIds(criteria.getStartDate(), criteria.getEndDate()))
                .thenReturn(Set.of(7L, 8L));
        Page<Vehicle> expected = new PageImpl<>(List.of(), pageable, 0);
        when(vehicleRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Vehicle>>any(), eq(pageable)))
                .thenReturn(expected);

        Page<Vehicle> result = service.handlePaged(new SearchVehiclesByCriteriaQuery(criteria), pageable);

        assertEquals(expected, result);
        verify(vehicleAvailabilityQueryService).findBlockedVehicleIds(criteria.getStartDate(), criteria.getEndDate());
    }

    @Test
    void handlePaged_withRadius_firstPage_evaluatesCandidatesUnpagedThenPaginatesInMemory() {
        SearchCriteria criteria = SearchCriteria.full(null, null, null, null, null, null, null, null, null,
                -12.05, -77.03, 10.0, null, null, null);
        Pageable pageable = PageRequest.of(0, 2);

        Vehicle inRange1 = mockVehicleAt(-12.05, -77.03);
        Vehicle inRange2 = mockVehicleAt(-12.06, -77.04);
        Vehicle inRange3 = mockVehicleAt(-12.04, -77.02);
        Vehicle outOfRange = mockVehicleAt(10.0, 10.0);
        Vehicle noCoordinates = mock(Vehicle.class);
        when(noCoordinates.getLatitude()).thenReturn(null);
        when(noCoordinates.getLongitude()).thenReturn(null);

        when(vehicleRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Vehicle>>any()))
                .thenReturn(List.of(inRange1, inRange2, inRange3, outOfRange, noCoordinates));

        Page<Vehicle> page1 = service.handlePaged(new SearchVehiclesByCriteriaQuery(criteria), pageable);

        assertEquals(3, page1.getTotalElements());
        assertEquals(2, page1.getTotalPages());
        assertEquals(2, page1.getContent().size());
        assertTrue(page1.getContent().containsAll(List.of(inRange1, inRange2)));

        // Never reaches the DB-level Pageable overload for the radius-filtered path.
        verify(vehicleRepository, never()).findAll(org.mockito.ArgumentMatchers.<Specification<Vehicle>>any(), any(Pageable.class));
    }

    @Test
    void handlePaged_withRadius_secondPage_returnsRemainingResultsWithoutDuplicationOrLoss() {
        SearchCriteria criteria = SearchCriteria.full(null, null, null, null, null, null, null, null, null,
                -12.05, -77.03, 10.0, null, null, null);

        Vehicle inRange1 = mockVehicleAt(-12.05, -77.03);
        Vehicle inRange2 = mockVehicleAt(-12.06, -77.04);
        Vehicle inRange3 = mockVehicleAt(-12.04, -77.02);

        when(vehicleRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Vehicle>>any()))
                .thenReturn(List.of(inRange1, inRange2, inRange3));

        Page<Vehicle> page2 = service.handlePaged(new SearchVehiclesByCriteriaQuery(criteria), PageRequest.of(1, 2));

        assertEquals(3, page2.getTotalElements());
        assertEquals(1, page2.getContent().size());
        assertEquals(inRange3, page2.getContent().get(0));
    }

    @Test
    void handlePaged_ownerQuery_delegatesToRepositoryPageableOverload() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Vehicle> expected = new PageImpl<>(List.of(), pageable, 0);
        when(vehicleRepository.findByOwnerId(eq(9L), eq(pageable))).thenReturn(expected);

        Page<Vehicle> result = service.handlePaged(new GetVehiclesByOwnerQuery(9L), pageable);

        assertEquals(expected, result);
    }

    private Vehicle mockVehicleAt(double lat, double lng) {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getLatitude()).thenReturn(BigDecimal.valueOf(lat));
        when(vehicle.getLongitude()).thenReturn(BigDecimal.valueOf(lng));
        return vehicle;
    }
}
