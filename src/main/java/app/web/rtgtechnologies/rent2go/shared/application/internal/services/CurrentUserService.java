package app.web.rtgtechnologies.rent2go.shared.application.internal.services;

import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * CurrentUserService
 *
 * Resolves the authenticated principal (populated by BearerAuthorizationRequestFilter as a
 * Spring Security {@link UserDetails} keyed by email/username) to the numeric {@code User} id,
 * and provides a single reusable ownership-check helper so controllers across bounded
 * contexts (payments, community_trust, notifications, ...) do not each reinvent the
 * "is this the resource owner or an admin" check.
 *
 * Controllers must not query {@link SecurityContextHolder} directly for authorization
 * decisions — inject this service instead.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the numeric id of the currently authenticated user, if any.
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentEmail()
                .flatMap(userRepository::findByEmail_Value)
                .map(app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User::getId);
    }

    /**
     * Returns true if the currently authenticated user holds ROLE_ADMIN.
     */
    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    /**
     * Returns true when the authenticated user is either the resource owner (matching id
     * against {@code resourceOwnerId}) or an admin. False (never throws) when there is no
     * authenticated user or the resource owner id cannot be matched.
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        if (resourceOwnerId == null) {
            return false;
        }
        if (isCurrentUserAdmin()) {
            return true;
        }
        return getCurrentUserId().map(resourceOwnerId::equals).orElse(false);
    }

    private Optional<String> getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }
        if (principal instanceof String s) {
            return Optional.of(s);
        }
        return Optional.empty();
    }
}
