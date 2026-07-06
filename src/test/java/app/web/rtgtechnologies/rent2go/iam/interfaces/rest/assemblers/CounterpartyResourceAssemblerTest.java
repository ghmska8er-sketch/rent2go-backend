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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
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

    private User mockUser(Long id, String fullName, boolean kycVerified, String profileImageUrl) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getFullName()).thenReturn(fullName);
        when(user.isKycVerified()).thenReturn(kycVerified);
        when(user.getProfileImageUrl()).thenReturn(profileImageUrl);
        return user;
    }
}
