package app.web.rtgtechnologies.rent2go.notifications.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByUserIdAndToken(Long userId, String token);
    Optional<DeviceToken> findByIdAndUserId(Long id, Long userId);
    List<DeviceToken> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<DeviceToken> findAllByUserIdAndEnabledTrueOrderByCreatedAtDesc(Long userId);
}