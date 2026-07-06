package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CounterpartyResourceAssembler
 *
 * Single source of truth for building a {@link CounterpartyResource} from a user id, joining
 * {@code User} (name, collapsed kycVerified flag, profile photo) with the most recent
 * {@code KycApplication} record (split dniVerified/licenseVerified booleans).
 *
 * Extracted from the near-duplicate {@code toCounterparty}/{@code toCounterpartyResource}/
 * {@code isKycApplicationApproved} methods that previously lived independently in both
 * {@code ReservationResourceFromEntityAssembler} (booking_reservations) and
 * {@code ConversationResourceFromEntityAssembler} (community_trust). Both now delegate here so
 * the KYC-join logic exists in exactly one place. Also reused by
 * {@code VehicleOwnerSummaryController} (vehicle_catalog) for the pre-booking
 * owner-identity lookup (US76 remaining scope, Sprint 5 fixes).
 *
 * Fail-open by design: a missing user or missing KycApplication never throws — always returns
 * an explicit fallback ("Usuario sin nombre registrado", false booleans, null photo), consistent
 * with this codebase's established defensive pattern (see BRD-2026-07-03 §8.3).
 *
 * Only fields already present on {@link CounterpartyResource} are ever populated here — never
 * email, phone, raw KycApplication document URLs, or the ID number (BRD-2026-07-05 §4.4/§7.2 R-004).
 */
@Component
@RequiredArgsConstructor
public class CounterpartyResourceAssembler {

    private static final String NO_NAME_ON_FILE = "Usuario sin nombre registrado";
    private static final String KYC_APPROVED = "APPROVED";

    private final UserRepository userRepository;
    private final KycApplicationRepository kycApplicationRepository;

    /**
     * Resolve a {@link CounterpartyResource} for the given user id.
     *
     * @param userId user id to resolve; {@code null} returns {@code null} (no counterparty).
     * @return a populated resource, or a "no name on file" fallback if the user id does not
     *         resolve to any {@code User} — never {@code null} when {@code userId} is non-null,
     *         never throws.
     */
    public CounterpartyResource toCounterparty(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
            .map(user -> toCounterpartyResource(user, isKycApplicationApproved(user.getId())))
            .orElse(new CounterpartyResource(userId, NO_NAME_ON_FILE, false, false, false, null));
    }

    /**
     * Perf fix (2026-07-06): batched counterpart of {@link #toCounterparty(Long)}, resolving
     * every distinct user id in {@code userIds} with exactly 2 queries total
     * ({@code userRepository.findAllById} + {@code kycApplicationRepository.findByUserIdIn})
     * instead of 2 queries PER user id. Used by list endpoints (e.g. GET /api/v1/reservations)
     * that previously enriched each row's renter and owner one at a time, multiplying the
     * per-row query count and dominating response time on paginated listings.
     *
     * @param userIds user ids to resolve; nulls and duplicates are ignored/deduplicated.
     * @return a map keyed by userId; missing users are NOT included (callers should fall back
     *         to a "no name on file" resource for absent keys, matching
     *         {@link #toCounterparty(Long)}'s fail-open behavior).
     */
    public Map<Long, CounterpartyResource> toCounterparties(Collection<Long> userIds) {
        Set<Long> distinctIds = userIds == null ? Set.of() : userIds.stream()
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());

        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        // Single batched query, then reduce to "most recent application per user" in memory —
        // mirroring findFirstByUserIdOrderByCreatedAtDesc's single-user semantics, but for the
        // whole page at once.
        Map<Long, List<KycApplication>> appsByUserId = kycApplicationRepository.findByUserIdIn(distinctIds).stream()
            .collect(Collectors.groupingBy(KycApplication::getUserId));

        Map<Long, Boolean> approvedByUserId = new HashMap<>();
        for (var entry : appsByUserId.entrySet()) {
            entry.getValue().stream()
                .max(Comparator.comparing(KycApplication::getCreatedAt))
                .ifPresent(mostRecent -> approvedByUserId.put(entry.getKey(), KYC_APPROVED.equalsIgnoreCase(mostRecent.getStatus())));
        }

        Map<Long, CounterpartyResource> result = new LinkedHashMap<>();
        for (User user : userRepository.findAllById(distinctIds)) {
            boolean verified = approvedByUserId.getOrDefault(user.getId(), false);
            result.put(user.getId(), toCounterpartyResource(user, verified));
        }
        return result;
    }

    private CounterpartyResource toCounterpartyResource(User user, boolean kycApplicationApproved) {
        String fullName = user.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = NO_NAME_ON_FILE;
        }
        return new CounterpartyResource(
            user.getId(),
            fullName,
            user.isKycVerified(),
            kycApplicationApproved,
            kycApplicationApproved,
            user.getProfileImageUrl()
        );
    }

    /**
     * Derives dniVerified/licenseVerified from the user's most recent {@code KycApplication}
     * record. {@code KycApplication} does not (yet) track DNI and driver's-license status as
     * two independently-tracked fields — both split badges are therefore identical today, both
     * sourced from the one existing collapsed {@code KycApplication.status}, per the
     * pre-resolved product decision (zero new tables, zero new per-document status columns).
     * Returns false, never throws, when no application exists on file.
     */
    private boolean isKycApplicationApproved(Long userId) {
        if (userId == null) {
            return false;
        }
        return kycApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
            .map(app -> KYC_APPROVED.equalsIgnoreCase(app.getStatus()))
            .orElse(false);
    }
}
