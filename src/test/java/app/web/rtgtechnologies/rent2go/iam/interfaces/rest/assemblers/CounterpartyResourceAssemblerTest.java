package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CounterpartyResourceAssemblerTest
 *
 * Covers the shared name/KYC-join logic extracted out of
 * ReservationResourceFromEntityAssembler / ConversationResourceFromEntityAssembler, now the
 * single source of truth also reused by the new vehicle owner-summary endpoint (US76 closure,
 * Sprint 5 fixes remaining scope).
 */
@ExtendWith(MockitoExtension.class)
class CounterpartyResourceAssemblerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KycApplicationRepository kycApplicationRepository;

    @InjectMocks
    private CounterpartyResourceAssembler assembler;

    @Test
    void nullUserIdReturnsNull() {
        assertEquals(null, assembler.toCounterparty(null));
    }

    @Test
    void fullDataCase_populatesAllFields() {
        User user = mockUser(1L, "Ana Torres", true, "https://cdn/ana.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        KycApplication approved = new KycApplication(1L, "Ana Torres", "12345678",
                "front.png", "back.png", "license.png", "APPROVED", Instant.now());
        when(kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(approved));

        var resource = assembler.toCounterparty(1L);

        assertNotNull(resource);
        assertEquals(1L, resource.id());
        assertEquals("Ana Torres", resource.fullName());
        assertEquals(Boolean.TRUE, resource.kycVerified());
        assertEquals(Boolean.TRUE, resource.dniVerified());
        assertEquals(Boolean.TRUE, resource.licenseVerified());
        assertEquals("https://cdn/ana.jpg", resource.profileImageUrl());
    }

    @Test
    void noKycApplicationCase_bothBadgesFalse_noException() {
        User user = mockUser(1L, "Ana Torres", false, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        var resource = assembler.toCounterparty(1L);

        assertNotNull(resource);
        assertFalse(resource.dniVerified());
        assertFalse(resource.licenseVerified());
        assertNull(resource.profileImageUrl());
    }

    @Test
    void userNotFound_returnsFallback_noException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        var resource = assembler.toCounterparty(99L);

        assertNotNull(resource);
        assertEquals(99L, resource.id());
        assertEquals("Usuario sin nombre registrado", resource.fullName());
        assertEquals(Boolean.FALSE, resource.kycVerified());
        assertEquals(Boolean.FALSE, resource.dniVerified());
        assertEquals(Boolean.FALSE, resource.licenseVerified());
        assertNull(resource.profileImageUrl());
    }

    @Test
    void blankFullName_fallsBackToNoNameOnFile() {
        User user = mockUser(1L, "   ", false, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var resource = assembler.toCounterparty(1L);

        assertEquals("Usuario sin nombre registrado", resource.fullName());
    }

    /**
     * Perf fix (2026-07-06): {@link CounterpartyResourceAssembler#toCounterparties(java.util.Collection)}
     * is the batched variant that removes the N+1 previously caused by resolving each
     * reservation-list row's renter/owner one at a time. This verifies it resolves multiple
     * distinct user ids with exactly ONE call to each repository (never once per user id).
     */
    @Test
    void toCounterparties_resolvesMultipleUsersWithOneBatchedQueryEach() {
        User renter = mockUser(1L, "Ana Torres", true, "https://cdn/ana.jpg");
        User owner = mockUser(2L, "Luis Ramos", false, null);
        when(userRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(renter, owner));
        KycApplication approvedForRenter = new KycApplication(1L, "Ana Torres", "12345678",
                "front.png", "back.png", "license.png", "APPROVED", Instant.now());
        when(kycApplicationRepository.findByUserIdIn(Set.of(1L, 2L))).thenReturn(List.of(approvedForRenter));

        Map<Long, app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource> result =
                assembler.toCounterparties(List.of(1L, 2L, 1L));

        assertEquals(2, result.size());
        assertEquals("Ana Torres", result.get(1L).fullName());
        assertEquals(Boolean.TRUE, result.get(1L).dniVerified());
        assertEquals("Luis Ramos", result.get(2L).fullName());
        assertEquals(Boolean.FALSE, result.get(2L).dniVerified());
        verify(userRepository).findAllById(anyCollection());
        verify(kycApplicationRepository).findByUserIdIn(anyCollection());
    }

    @Test
    void toCounterparties_withNullOrEmptyInput_returnsEmptyMapWithoutQuerying() {
        assertTrue(assembler.toCounterparties(null).isEmpty());
        assertTrue(assembler.toCounterparties(List.of()).isEmpty());
        verify(userRepository, never()).findAllById(anyCollection());
        verify(kycApplicationRepository, never()).findByUserIdIn(anyCollection());
    }

    @Test
    void toCounterparties_userWithNoKycApplication_defaultsBadgesToFalse() {
        User renter = mockUser(1L, "Ana Torres", false, null);
        when(userRepository.findAllById(Set.of(1L))).thenReturn(List.of(renter));
        when(kycApplicationRepository.findByUserIdIn(Set.of(1L))).thenReturn(List.of());

        var result = assembler.toCounterparties(List.of(1L));

        assertEquals(Boolean.FALSE, result.get(1L).dniVerified());
        assertEquals(Boolean.FALSE, result.get(1L).licenseVerified());
    }

    private User mockUser(Long id, String fullName, boolean kycVerified, String profileImageUrl) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getFullName()).thenReturn(fullName);
        when(user.isKycVerified()).thenReturn(kycVerified);
        when(user.getProfileImageUrl()).thenReturn(profileImageUrl);
        return user;
    }
}
