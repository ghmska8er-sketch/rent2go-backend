package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers.CounterpartyResourceAssembler;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * TS18 — verifies ReservationResourceFromEntityAssembler embeds a nested counterparty object
 * (renter/owner) alongside the existing bare renterId/ownerId fields, following the
 * community_trust -> iam cross-context read pattern.
 *
 * Sprint 5 (US76/TS23) — extended to also cover vehicleImage and the split KYC/profile-photo
 * enrichment, including the explicit null/missing-data cases the BRD requires (no vehicle
 * image, no KycApplication record, no profile photo) — never an exception, always a fallback.
 *
 * Sprint 5 follow-up: the name/KYC-join logic now lives in the shared
 * {@link CounterpartyResourceAssembler} (extracted, single source of truth) — this test wires
 * a real instance of it (backed by mocked repositories) rather than mocking the join logic
 * itself, since {@code ReservationResourceFromEntityAssembler} now only delegates for that part.
 */
@ExtendWith(MockitoExtension.class)
class ReservationResourceFromEntityAssemblerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private KycApplicationRepository kycApplicationRepository;

    private ReservationResourceFromEntityAssembler assembler;

    @BeforeEach
    void setUp() {
        CounterpartyResourceAssembler counterpartyResourceAssembler =
                new CounterpartyResourceAssembler(userRepository, kycApplicationRepository);
        assembler = new ReservationResourceFromEntityAssembler(vehicleRepository, counterpartyResourceAssembler);
    }

    private Reservation reservation(Long renterId, Long ownerId) {
        return Reservation.create(
            10L,
            renterId,
            ownerId,
            DateRange.of(LocalDate.now(), LocalDate.now().plusDays(2)),
            BigDecimal.valueOf(150)
        );
    }

    @Test
    void embedsNestedCounterpartyWithFullNameAndKycStatus() {
        User renter = mockUser(1L, "Ana Torres", true);
        User owner = mockUser(2L, "Luis Ramos", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        var resource = assembler.toResource(reservation(1L, 2L));

        assertEquals(1L, resource.renterId());
        assertEquals(2L, resource.ownerId());
        assertNotNull(resource.renter());
        assertEquals("Ana Torres", resource.renter().fullName());
        assertEquals(Boolean.TRUE, resource.renter().kycVerified());
        assertNotNull(resource.owner());
        assertEquals("Luis Ramos", resource.owner().fullName());
        assertEquals(Boolean.FALSE, resource.owner().kycVerified());
    }

    @Test
    void fallsBackToNoNameOnFileWhenFullNameIsBlank() {
        User renter = mockUser(1L, "  ", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        var resource = assembler.toResource(reservation(1L, 2L));

        assertEquals("Usuario sin nombre registrado", resource.renter().fullName());
        assertEquals("Usuario sin nombre registrado", resource.owner().fullName());
        assertEquals(Boolean.FALSE, resource.owner().kycVerified());
    }

    @Test
    void neverThrowsWhenUserLookupIsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        var resource = assembler.toResource(reservation(1L, 2L));

        assertNotNull(resource.renter());
        assertNotNull(resource.owner());
    }

    @Test
    void fullDataCase_embedsVehicleImageAndSplitKycBadgesAndProfilePhoto() {
        User renter = mockUser(1L, "Ana Torres", true, "https://cdn/ana.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        KycApplication approved = new KycApplication(1L, "Ana Torres", "12345678",
                "front.png", "back.png", "license.png", "APPROVED", java.time.Instant.now());
        when(kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(approved));
        Vehicle vehicle = org.mockito.Mockito.mock(Vehicle.class);
        when(vehicle.getPrimaryImageUrl()).thenReturn("https://cdn/vehicle.jpg");
        when(vehicleRepository.findById(10L)).thenReturn(Optional.empty());

        var reservation = reservation(1L, 2L);
        when(vehicleRepository.findById(reservation.getVehicleId())).thenReturn(Optional.of(vehicle));

        var resource = assembler.toResource(reservation);

        assertEquals("https://cdn/vehicle.jpg", resource.vehicleImage());
        assertEquals(Boolean.TRUE, resource.renter().dniVerified());
        assertEquals(Boolean.TRUE, resource.renter().licenseVerified());
        assertEquals("https://cdn/ana.jpg", resource.renter().profileImageUrl());
    }

    @Test
    void noKycApplicationCase_bothBadgesFalse_noException() {
        User renter = mockUser(1L, "Ana Torres", false, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        var resource = assembler.toResource(reservation(1L, 2L));

        assertEquals(Boolean.FALSE, resource.renter().dniVerified());
        assertEquals(Boolean.FALSE, resource.renter().licenseVerified());
    }

    @Test
    void noVehicleImageCase_vehicleImageIsNull_noException() {
        var reservation = reservation(1L, 2L);
        when(vehicleRepository.findById(reservation.getVehicleId())).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        var resource = assembler.toResource(reservation);

        assertNull(resource.vehicleImage());
    }

    @Test
    void noProfilePhotoCase_profileImageUrlIsNull_noException() {
        User renter = mockUser(1L, "Ana Torres", false, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        var resource = assembler.toResource(reservation(1L, 2L));

        assertNull(resource.renter().profileImageUrl());
    }

    private User mockUser(Long id, String fullName, boolean kycVerified) {
        return mockUser(id, fullName, kycVerified, null);
    }

    private User mockUser(Long id, String fullName, boolean kycVerified, String profileImageUrl) {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getFullName()).thenReturn(fullName);
        when(user.isKycVerified()).thenReturn(kycVerified);
        when(user.getProfileImageUrl()).thenReturn(profileImageUrl);
        return user;
    }
}
