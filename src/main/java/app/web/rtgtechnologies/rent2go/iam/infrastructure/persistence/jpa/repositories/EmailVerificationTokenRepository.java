package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
