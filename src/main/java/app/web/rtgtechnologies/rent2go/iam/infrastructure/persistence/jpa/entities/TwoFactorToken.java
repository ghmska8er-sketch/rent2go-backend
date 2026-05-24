package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "two_factor_tokens")
public class TwoFactorToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "purpose", nullable = false)
    private String purpose; // ENABLE or LOGIN

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TwoFactorToken() {}

    public TwoFactorToken(String token, Long userId, String purpose, Instant expiresAt, Instant createdAt) {
        this.token = token;
        this.userId = userId;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getPurpose() { return purpose; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
