package app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReviewStatus extends ValueObject {

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public static ReviewStatus PENDING() {
        return new ReviewStatus("PENDING");
    }

    public static ReviewStatus APPROVED() {
        return new ReviewStatus("APPROVED");
    }

    public static ReviewStatus REJECTED() {
        return new ReviewStatus("REJECTED");
    }

    public static ReviewStatus FLAGGED() {
        return new ReviewStatus("FLAGGED");
    }

    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isApproved() {
        return "APPROVED".equals(this.status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(this.status);
    }

    public boolean isFlagged() {
        return "FLAGGED".equals(this.status);
    }

    public boolean isTerminal() {
        return isApproved() || isRejected();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReviewStatus that = (ReviewStatus) o;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}