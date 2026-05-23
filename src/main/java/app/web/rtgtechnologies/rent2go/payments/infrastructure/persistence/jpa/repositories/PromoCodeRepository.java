package app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeAndActiveTrue(String code);
}
