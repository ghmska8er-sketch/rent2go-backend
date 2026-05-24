package app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByConversationIdOrderByCreatedAtAsc(Long conversationId);
}