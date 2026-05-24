package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitReviewResource(
    @NotNull(message = "Reservation id is required") Long reservationId,
    @NotNull(message = "Vehicle id is required") Long vehicleId,
    @NotNull(message = "Reviewer id is required") Long reviewerId,
    Long reviewedUserId,
    @NotNull(message = "Category is required") ReviewCategory category,
    @NotNull(message = "Rating is required") @Min(value = 1, message = "Rating must be at least 1") @Max(value = 5, message = "Rating must be at most 5") Integer rating,
    @Size(max = 1000, message = "Comment must be at most 1000 characters") String comment
) {
}