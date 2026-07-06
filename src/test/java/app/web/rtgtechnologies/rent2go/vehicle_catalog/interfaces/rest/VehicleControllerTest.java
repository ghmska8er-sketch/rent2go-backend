package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest;

import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers.CounterpartyResourceAssembler;
import app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary.CloudinaryStorageService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleCategory;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.GetVehiclesByOwnerQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.queries.SearchVehiclesByCriteriaQuery;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleCommandService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleQueryService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleCategoryRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VehicleControllerTest
 *
 * Sprint 5 (TS22, BRD-2026-07-05-Paginacion-Real-Backend-Vehiculos.md): confirms
 * GET /api/v1/vehicles and GET /api/v1/vehicles/me delegate to the new
 * VehicleQueryService.handlePaged(...) overloads with a correctly-converted Pageable, and
 * that the resulting PagedResponse preserves the exact pre-existing contract shape
 * (content/page/size/totalElements/totalPages) with no client-visible change — specifically,
 * that this controller's historical 1-indexed `page` parameter still round-trips correctly
 * through the new 0-indexed Spring Data Pageable conversion.
 */
@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock private VehicleCommandService vehicleCommandService;
    @Mock private VehicleQueryService vehicleQueryService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private CloudinaryStorageService cloudinaryStorageService;
    @Mock private VehicleCategoryRepository vehicleCategoryRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;
    @Mock private KycApplicationRepository kycApplicationRepository;

    private VehicleController controller;

    @BeforeEach
    void setUp() {
        CounterpartyResourceAssembler counterpartyResourceAssembler =
                new CounterpartyResourceAssembler(userRepository, kycApplicationRepository);
        controller = new VehicleController(
                vehicleCommandService,
                vehicleQueryService,
                jwtTokenProvider,
                cloudinaryStorageService,
                vehicleCategoryRepository,
                vehicleRepository,
                reservationRepository,
                counterpartyResourceAssembler
        );
    }

    private Vehicle mockVehicle(Long id) {
        Vehicle vehicle = mock(Vehicle.class);
        VehicleCategory category = mock(VehicleCategory.class);
        when(category.getName()).thenReturn("SUV");
        when(vehicle.getId()).thenReturn(id);
        when(vehicle.getOwnerId()).thenReturn(1L);
        when(vehicle.getLicensePlate()).thenReturn("ABC-123");
        when(vehicle.getMake()).thenReturn("Toyota");
        when(vehicle.getModel()).thenReturn("RAV4");
        when(vehicle.getYear()).thenReturn(2022);
        when(vehicle.getVin()).thenReturn("VIN" + id);
        when(vehicle.getStatus()).thenReturn(VehicleStatus.AVAILABLE);
        when(vehicle.getDailyPrice()).thenReturn(BigDecimal.valueOf(100));
        when(vehicle.getCategory()).thenReturn(category);
        when(vehicle.getFeatures()).thenReturn(List.of());
        return vehicle;
    }

    @Test
    void searchAvailableVehicles_convertsOneIndexedPageToZeroIndexedPageable_andPreservesContractShape() {
        Vehicle vehicle = mockVehicle(10L);
        Pageable expectedPageable = PageRequest.of(2, 5); // client page=3 (1-indexed) -> Pageable page=2
        Page<Vehicle> page = new PageImpl<>(List.of(vehicle), expectedPageable, 11);
        when(vehicleQueryService.handlePaged(any(SearchVehiclesByCriteriaQuery.class), eq(expectedPageable)))
                .thenReturn(page);

        var response = controller.searchAvailableVehicles(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, 3, 5);

        assertEquals(200, response.getStatusCode().value());
        var body = response.getBody();
        assertEquals(1, body.content().size());
        assertEquals(3, body.page());
        assertEquals(5, body.size());
        assertEquals(11, body.totalElements());
        assertEquals(3, body.totalPages());
    }

    @Test
    void searchAvailableVehicles_noFilters_behavesIdenticallyToBeforeMigration() {
        Vehicle v1 = mockVehicle(1L);
        Vehicle v2 = mockVehicle(2L);
        Pageable expectedPageable = PageRequest.of(0, 20);
        Page<Vehicle> page = new PageImpl<>(List.of(v1, v2), expectedPageable, 2);
        when(vehicleQueryService.handlePaged(any(SearchVehiclesByCriteriaQuery.class), eq(expectedPageable)))
                .thenReturn(page);

        var response = controller.searchAvailableVehicles(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, 1, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().content().size());
        assertEquals(2, response.getBody().totalElements());
        assertEquals(1, response.getBody().totalPages());
    }

    @Test
    void getMyVehicles_delegatesToHandlePagedWithOwnerIdFromToken() {
        when(jwtTokenProvider.extractUserIdFromToken("token123")).thenReturn(7L);
        Vehicle vehicle = mockVehicle(20L);
        Pageable expectedPageable = PageRequest.of(0, 20);
        Page<Vehicle> page = new PageImpl<>(List.of(vehicle), expectedPageable, 1);
        when(vehicleQueryService.handlePaged(eq(new GetVehiclesByOwnerQuery(7L)), eq(expectedPageable)))
                .thenReturn(page);

        var response = controller.getMyVehicles("Bearer token123", 1, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().content().size());
        assertEquals(1, response.getBody().totalElements());
        verify(vehicleQueryService).handlePaged(eq(new GetVehiclesByOwnerQuery(7L)), eq(expectedPageable));
    }

    @Test
    void searchAvailableVehicles_emptyResult_returnsZeroTotalPagesNotError() {
        Pageable expectedPageable = PageRequest.of(0, 20);
        Page<Vehicle> page = new PageImpl<>(List.of(), expectedPageable, 0);
        when(vehicleQueryService.handlePaged(any(SearchVehiclesByCriteriaQuery.class), eq(expectedPageable)))
                .thenReturn(page);

        var response = controller.searchAvailableVehicles(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, 1, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().content().size());
        assertEquals(0, response.getBody().totalElements());
        assertEquals(0, response.getBody().totalPages());
    }

    // ---- GET /api/v1/vehicles/{id}/owner-summary (US76 closure, Sprint 5 fixes remaining scope) ----

    @Test
    void getVehicleOwnerSummary_fullDataCase_returnsNameVerificationBadgesAndPhoto() {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getOwnerId()).thenReturn(5L);
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(5L);
        when(owner.getFullName()).thenReturn("Luis Ramos");
        when(owner.isKycVerified()).thenReturn(true);
        when(owner.getProfileImageUrl()).thenReturn("https://cdn/luis.jpg");
        when(userRepository.findById(5L)).thenReturn(Optional.of(owner));

        KycApplication approved = new KycApplication(5L, "Luis Ramos", "87654321",
                "front.png", "back.png", "license.png", "APPROVED", Instant.now());
        when(kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(5L))
                .thenReturn(Optional.of(approved));

        var response = controller.getVehicleOwnerSummary(10L);

        assertEquals(200, response.getStatusCode().value());
        var body = response.getBody();
        assertEquals(5L, body.id());
        assertEquals("Luis Ramos", body.fullName());
        assertEquals(Boolean.TRUE, body.kycVerified());
        assertEquals(Boolean.TRUE, body.dniVerified());
        assertEquals(Boolean.TRUE, body.licenseVerified());
        assertEquals("https://cdn/luis.jpg", body.profileImageUrl());
    }

    @Test
    void getVehicleOwnerSummary_noKycApplicationCase_badgesFalse_noException() {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getOwnerId()).thenReturn(6L);
        when(vehicleRepository.findById(11L)).thenReturn(Optional.of(vehicle));

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(6L);
        when(owner.getFullName()).thenReturn("Owner Sin Kyc");
        when(owner.isKycVerified()).thenReturn(false);
        when(owner.getProfileImageUrl()).thenReturn(null);
        when(userRepository.findById(6L)).thenReturn(Optional.of(owner));
        when(kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(6L))
                .thenReturn(Optional.empty());

        var response = controller.getVehicleOwnerSummary(11L);

        assertEquals(200, response.getStatusCode().value());
        var body = response.getBody();
        assertFalse(body.dniVerified());
        assertFalse(body.licenseVerified());
        assertNull(body.profileImageUrl());
    }

    @Test
    void getVehicleOwnerSummary_vehicleNotFound_returns404() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        var response = controller.getVehicleOwnerSummary(999L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getVehicleOwnerSummary_neverLeaksEmailOrPhone() {
        // CounterpartyResource is a record with a fixed, closed set of components — this test
        // asserts that set directly, so any future accidental addition of email/phone to the
        // record is caught here rather than only being caught by an eyeball code review.
        var componentNames = java.util.Arrays.stream(
                        app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource.class
                                .getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(
                java.util.Set.of("id", "fullName", "kycVerified", "dniVerified", "licenseVerified", "profileImageUrl"),
                componentNames
        );
        assertFalse(componentNames.stream().anyMatch(n -> n.toLowerCase().contains("email")));
        assertFalse(componentNames.stream().anyMatch(n -> n.toLowerCase().contains("phone")));
    }
}
