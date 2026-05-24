package app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.TrustReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustReportRepository extends JpaRepository<TrustReport, Long> {

    List<TrustReport> findAllByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId);

    List<TrustReport> findAllByReviewId(Long reviewId);
}