package app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.RatingValue;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewCategory;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewStatus;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
public class Review extends AuditableAbstractAggregateRoot<Review> {

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "reviewed_user_id")
    private Long reviewedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ReviewCategory category;

    @Embedded
    private RatingValue rating;

    @Embedded
    private ReviewStatus status;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "flag_count", nullable = false)
    private Integer flagCount;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;

    private Review(Long reservationId,
                   Long vehicleId,
                   Long reviewerId,
                   Long reviewedUserId,
                   ReviewCategory category,
                   RatingValue rating,
                   String comment) {
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.reviewerId = reviewerId;
        this.reviewedUserId = reviewedUserId;
        this.category = category;
        this.rating = rating;
        this.comment = comment;
        this.status = ReviewStatus.PENDING();
        this.flagCount = 0;
    }

    public static Review submit(Long reservationId,
                                Long vehicleId,
                                Long reviewerId,
                                Long reviewedUserId,
                                ReviewCategory category,
                                RatingValue rating,
                                String comment) {
        if (reservationId == null || vehicleId == null || reviewerId == null || category == null || rating == null) {
            throw new IllegalArgumentException("reservationId, vehicleId, reviewerId, category and rating are required");
        }

        return new Review(reservationId, vehicleId, reviewerId, reviewedUserId, category, rating, comment);
    }

    public void approve(String moderationNote) {
        this.status = ReviewStatus.APPROVED();
        this.moderationNote = moderationNote;
    }

    public void reject(String moderationNote) {
        this.status = ReviewStatus.REJECTED();
        this.moderationNote = moderationNote;
    }

    public void flag(String moderationNote) {
        this.flagCount = this.flagCount + 1;
        this.status = ReviewStatus.FLAGGED();
        this.moderationNote = moderationNote;
    }

    public boolean isApproved() {
        return this.status != null && this.status.isApproved();
    }

    public boolean isRejected() {
        return this.status != null && this.status.isRejected();
    }

    public boolean isFlagged() {
        return this.status != null && this.status.isFlagged();
    }

    public Integer getRatingValue() {
        return this.rating == null ? null : this.rating.getValue();
    }
}