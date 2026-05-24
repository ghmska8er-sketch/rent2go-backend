package app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Entity
@Table(name = "user_reputations")
@Getter
@NoArgsConstructor
public class UserReputation extends AuditableAbstractAggregateRoot<UserReputation> {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "approved_review_count", nullable = false)
    private Integer approvedReviewCount;

    @Column(name = "average_rating", nullable = false, precision = 5, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "trust_score", nullable = false)
    private Integer trustScore;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    @Column(name = "last_moderation_reason", length = 500)
    private String lastModerationReason;

    private UserReputation(Long userId) {
        this.userId = userId;
        this.approvedReviewCount = 0;
        this.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.trustScore = 0;
        this.blocked = false;
    }

    public static UserReputation forUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        return new UserReputation(userId);
    }

    public void recalculate(List<Review> reviews) {
        long approvedCount = reviews.stream().filter(Review::isApproved).count();
        BigDecimal sum = reviews.stream()
            .filter(Review::isApproved)
            .map(Review::getRatingValue)
            .filter(value -> value != null)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = approvedCount > 0
            ? sum.divide(BigDecimal.valueOf(approvedCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        long penalties = reviews.stream().filter(review -> review.isRejected() || review.isFlagged()).count();
        int score = average.multiply(BigDecimal.valueOf(20)).intValue()
            + (int) Math.min(approvedCount * 2, 20)
            - (int) Math.min(penalties * 10, 40);

        this.approvedReviewCount = (int) approvedCount;
        this.averageRating = average;
        this.trustScore = Math.max(0, Math.min(100, score));
    }

    public void block(String reason) {
        this.blocked = true;
        this.lastModerationReason = reason;
        this.trustScore = 0;
    }

    public void unblock() {
        this.blocked = false;
    }
}