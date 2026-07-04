package app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.CreateNotificationCommand;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification
 *
 * TS11/US50/US51/US52: an in-app notification entry for a user. Per SP05's decision
 * (docs/spikes/SP05-push-notification-scope.md), Sprint 3 implements in-app-only
 * notifications — no FCM/APNs push is sent when a Notification is created here.
 * {@link #readAt} is null while unread; set once via {@link #markAsRead()}.
 */
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
public class Notification extends AuditableAbstractAggregateRoot<Notification> {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public Notification(CreateNotificationCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (command.type() == null || command.type().isBlank()) {
            throw new IllegalArgumentException("type is required");
        }
        if (command.message() == null || command.message().isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
        this.userId = command.userId();
        this.type = command.type();
        this.message = command.message();
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    public boolean isUnread() {
        return this.readAt == null;
    }
}
