package app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor
public class Message extends AuditableAbstractAggregateRoot<Message> {

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "read_at")
    private Instant readAt;

    private Message(Long conversationId, Long senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
    }

    public static Message send(Long conversationId, Long senderId, String content) {
        if (conversationId == null || senderId == null || content == null || content.isBlank()) {
            throw new IllegalArgumentException("conversationId, senderId and content are required");
        }

        return new Message(conversationId, senderId, content.trim());
    }

    public void markAsRead() {
        this.readAt = Instant.now();
    }
}