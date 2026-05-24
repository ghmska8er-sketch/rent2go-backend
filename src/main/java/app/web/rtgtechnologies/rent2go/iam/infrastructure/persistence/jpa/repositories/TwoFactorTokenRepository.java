package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.TwoFactorToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TwoFactorTokenRepository extends JpaRepository<TwoFactorToken, Long> {
    Optional<TwoFactorToken> findByToken(String token);
    void deleteByUserIdAndPurpose(Long userId, String purpose);
}
