package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplication, Long> {
    Optional<KycApplication> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Sprint 5 (US76/TS23, BRD-2026-07-05-Reservation-Vehicle-Counterparty-Enrichment.md):
     * batched lookup for a set of counterparty user IDs, to avoid an N+1 query when a
     * reservation-list endpoint enriches many counterparties at once. Callers must reduce
     * this to "most recent per user" themselves (grouping by userId, keeping the max
     * createdAt), since a derived query cannot express that reduction per-group.
     */
    List<KycApplication> findByUserIdIn(Collection<Long> userIds);
}
