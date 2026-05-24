package app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.UserReputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReputationRepository extends JpaRepository<UserReputation, Long> {

    Optional<UserReputation> findByUserId(Long userId);
}