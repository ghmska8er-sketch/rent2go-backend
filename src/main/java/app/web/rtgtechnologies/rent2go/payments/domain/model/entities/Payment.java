package app.web.rtgtechnologies.rent2go.payments.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "amount_cents")
    private Long amountCents;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payment(Long reservationId, String paymentIntentId, Long amountCents, String currency, String status) {
        this.reservationId = reservationId;
        this.paymentIntentId = paymentIntentId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
