package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * TS18 — verifies ReservationResourceFromEntityAssembler embeds a nested counterparty object
 * (renter/owner) alongside the existing bare renterId/ownerId fields, following the
 * community_trust -> iam cross-context read pattern.
 */
@ExtendWith(MockitoExtension.class)
class ReservationResourceFromEntityAssemblerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationResourceFromEntityAssembler assembler;

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

    private User mockUser(Long id, String fullName, boolean kycVerified) {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getFullName()).thenReturn(fullName);
        when(user.isKycVerified()).thenReturn(kycVerified);
        return user;
    }
}
