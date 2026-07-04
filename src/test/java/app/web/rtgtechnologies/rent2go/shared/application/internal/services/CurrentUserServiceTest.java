package app.web.rtgtechnologies.rent2go.shared.application.internal.services;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * CurrentUserServiceTest
 *
 * Unit tests for the shared ownership-check helper used by Phase 1's earnings-ownership fix
 * and Phase 2/3/4's other ownership-checked endpoints. All IO (UserRepository) is mocked;
 * authentication is simulated directly via SecurityContextHolder, no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private CurrentUserService service;

    @BeforeEach
    void setUp() {
        service = new CurrentUserService(userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isOwnerOrAdmin_returnsTrue_whenAuthenticatedUserMatchesOwner() {
        authenticateAs("owner@example.com", "ROLE_USER");
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(userRepository.findByEmail_Value("owner@example.com")).thenReturn(Optional.of(user));

        assertTrue(service.isOwnerOrAdmin(42L));
    }

    @Test
    void isOwnerOrAdmin_returnsFalse_whenAuthenticatedUserIsDifferentPerson() {
        authenticateAs("someone-else@example.com", "ROLE_USER");
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(99L);
        when(userRepository.findByEmail_Value("someone-else@example.com")).thenReturn(Optional.of(user));

        assertFalse(service.isOwnerOrAdmin(42L));
    }

    @Test
    void isOwnerOrAdmin_returnsTrue_whenAuthenticatedUserIsAdmin_evenIfNotOwner() {
        authenticateAs("admin@example.com", "ROLE_ADMIN");

        assertTrue(service.isOwnerOrAdmin(42L));
    }

    @Test
    void isOwnerOrAdmin_returnsFalse_whenNoAuthenticationPresent() {
        SecurityContextHolder.clearContext();

        assertFalse(service.isOwnerOrAdmin(42L));
    }

    private void authenticateAs(String email, String role) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("irrelevant")
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
