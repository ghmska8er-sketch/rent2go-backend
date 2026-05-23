package app.web.rtgtechnologies.rent2go.payments.domain.model.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal percentage;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime expiresAt;

    public PromoCode() {}

    public PromoCode(String code, BigDecimal percentage, boolean active, LocalDateTime expiresAt) {
        this.code = code;
        this.percentage = percentage;
        this.active = active;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public BigDecimal getPercentage() { return percentage; }
    public boolean isActive() { return active; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setActive(boolean active) { this.active = active; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
