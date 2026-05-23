package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.PromoCode;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Discount;
import app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories.PromoCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.math.BigDecimal;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PromoService {

    private final PromoCodeRepository repo;

    public PromoService(PromoCodeRepository repo) {
        this.repo = repo;
    }

    public Optional<Discount> findActiveDiscountByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return repo.findByCodeAndActiveTrue(code.trim()).flatMap(p -> {
            if (p.getExpiresAt() != null && p.getExpiresAt().isBefore(LocalDateTime.now())) return Optional.empty();
            return Optional.of(Discount.of(p.getCode(), p.getPercentage()));
        });
    }

    @Transactional
    public PromoCode createPromo(String code, BigDecimal percentage, LocalDateTime expiresAt) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code required");
        if (percentage == null) percentage = BigDecimal.ZERO;
        PromoCode p = new PromoCode(code.trim().toUpperCase(), percentage, true, expiresAt);
        return repo.save(p);
    }

    @Transactional
    public boolean deactivateByCode(String code) {
        var opt = repo.findByCodeAndActiveTrue(code);
        if (opt.isEmpty()) return false;
        var p = opt.get();
        p.setActive(false);
        repo.save(p);
        return true;
    }
}
