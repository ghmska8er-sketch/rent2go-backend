package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Conversation;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers.CounterpartyResourceAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TS18 — verifies ConversationResourceFromEntityAssembler embeds a nested counterparty object
 * (owner/renter) alongside the existing bare ownerId/renterId fields.
 *
 * Sprint 5 follow-up: the actual name/KYC-join logic now lives in the shared
 * {@link CounterpartyResourceAssembler} (extracted, single source of truth) — this test wires
 * a real instance of it (backed by mocked repositories) rather than mocking the join logic
 * itself, since {@code ConversationResourceFromEntityAssembler} now only delegates.
 */
@ExtendWith(MockitoExtension.class)
class ConversationResourceFromEntityAssemblerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KycApplicationRepository kycApplicationRepository;

    private ConversationResourceFromEntityAssembler assembler;

    @BeforeEach
    void setUp() {
        CounterpartyResourceAssembler counterpartyResourceAssembler =
                new CounterpartyResourceAssembler(userRepository, kycApplicationRepository);
        assembler = new ConversationResourceFromEntityAssembler(counterpartyResourceAssembler);
    }

    @Test
    void embedsNestedCounterpartyWithFullNameAndKycStatus() {
        Conversation conversation = Conversation.start(2L, 1L, 5L, null, "Consulta sobre el vehiculo");
        User owner = mockUser(2L, "Luis Ramos", true);
        User renter = mockUser(1L, "Ana Torres", false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));

        var resource = assembler.toResource(conversation);

        assertEquals(2L, resource.ownerId());
        assertEquals(1L, resource.renterId());
        assertNotNull(resource.owner());
        assertEquals("Luis Ramos", resource.owner().fullName());
        assertEquals(Boolean.TRUE, resource.owner().kycVerified());
        assertNotNull(resource.renter());
        assertEquals("Ana Torres", resource.renter().fullName());
    }

    @Test
    void splitVerificationBadgesReflectMostRecentKycApplicationStatus() {
        Conversation conversation = Conversation.start(2L, 1L, 5L, null, "Consulta sobre el vehiculo");
        User owner = mockUser(2L, "Luis Ramos", true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        KycApplication approved = new KycApplication(2L, "Luis Ramos", "12345678",
                "front.png", "back.png", "license.png", "APPROVED", java.time.Instant.now());
        when(kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(2L))
                .thenReturn(Optional.of(approved));

        var resource = assembler.toResource(conversation);

        assertEquals(Boolean.TRUE, resource.owner().dniVerified());
        assertEquals(Boolean.TRUE, resource.owner().licenseVerified());
        assertEquals(Boolean.FALSE, resource.renter().dniVerified());
        assertEquals(Boolean.FALSE, resource.renter().licenseVerified());
    }

    @Test
    void fallsBackToNoNameOnFileWhenUserNotFound() {
        Conversation conversation = Conversation.start(2L, 1L, 5L, null, "Consulta");
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        var resource = assembler.toResource(conversation);

        assertEquals("Usuario sin nombre registrado", resource.owner().fullName());
        assertEquals("Usuario sin nombre registrado", resource.renter().fullName());
    }

    private User mockUser(Long id, String fullName, boolean kycVerified) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getFullName()).thenReturn(fullName);
        when(user.isKycVerified()).thenReturn(kycVerified);
        return user;
    }
}
