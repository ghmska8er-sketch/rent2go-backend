package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplication, Long> {
    Optional<KycApplication> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
