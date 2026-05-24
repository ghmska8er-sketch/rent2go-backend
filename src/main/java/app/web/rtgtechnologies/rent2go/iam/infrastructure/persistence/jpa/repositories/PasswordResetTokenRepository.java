package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
