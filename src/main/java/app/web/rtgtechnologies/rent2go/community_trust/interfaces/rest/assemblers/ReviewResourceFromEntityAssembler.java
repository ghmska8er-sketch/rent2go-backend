package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Review;
import app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources.ReviewResource;
import org.springframework.stereotype.Component;

@Component
public class ReviewResourceFromEntityAssembler {

    public ReviewResource toResource(Review review) {
        return new ReviewResource(
            review.getId(),
            review.getReservationId(),
            review.getVehicleId(),
            review.getReviewerId(),
            review.getReviewedUserId(),
            review.getCategory(),
            review.getRatingValue(),
            review.getStatus() == null ? null : review.getStatus().getStatus(),
            review.getComment(),
            review.getFlagCount(),
            review.getModerationNote(),
            review.getCreatedAt() == null ? null : review.getCreatedAt().toString(),
            review.getUpdatedAt() == null ? null : review.getUpdatedAt().toString()
        );
    }
}