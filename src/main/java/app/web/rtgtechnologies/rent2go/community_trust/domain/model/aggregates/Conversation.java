package app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ConversationStatus;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "conversations")
@Getter
@NoArgsConstructor
public class Conversation extends AuditableAbstractAggregateRoot<Conversation> {

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "subject", length = 120)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConversationStatus status;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "last_message_preview", length = 500)
    private String lastMessagePreview;

    private Conversation(Long ownerId,
                         Long renterId,
                         Long vehicleId,
                         Long reservationId,
                         String subject) {
        this.ownerId = ownerId;
        this.renterId = renterId;
        this.vehicleId = vehicleId;
        this.reservationId = reservationId;
        this.subject = subject;
        this.status = ConversationStatus.OPEN;
    }

    public static Conversation start(Long ownerId,
                                     Long renterId,
                                     Long vehicleId,
                                     Long reservationId,
                                     String subject) {
        if (ownerId == null || renterId == null) {
            throw new IllegalArgumentException("ownerId and renterId are required");
        }

        if (ownerId.equals(renterId)) {
            throw new IllegalArgumentException("ownerId and renterId must be different");
        }

        return new Conversation(ownerId, renterId, vehicleId, reservationId, subject == null ? null : subject.trim());
    }

    public void touchMessage(String preview) {
        this.lastMessageAt = Instant.now();
        this.lastMessagePreview = preview == null ? null : preview.trim();
    }

    public void close() {
        this.status = ConversationStatus.CLOSED;
    }

    public boolean belongsTo(Long userId) {
        return userId != null && (userId.equals(this.ownerId) || userId.equals(this.renterId));
    }
}