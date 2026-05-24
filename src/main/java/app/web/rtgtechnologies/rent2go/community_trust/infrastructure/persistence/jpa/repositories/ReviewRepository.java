package app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Review;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<Review> findAllByReviewedUserIdOrderByCreatedAtDesc(Long reviewedUserId);

    List<Review> findAllByReviewedUserId(Long reviewedUserId);

    List<Review> findAllByVehicleId(Long vehicleId);

    boolean existsByReservationIdAndReviewerIdAndCategory(Long reservationId, Long reviewerId, ReviewCategory category);

    Optional<Review> findByReservationIdAndReviewerIdAndCategory(Long reservationId, Long reviewerId, ReviewCategory category);
}