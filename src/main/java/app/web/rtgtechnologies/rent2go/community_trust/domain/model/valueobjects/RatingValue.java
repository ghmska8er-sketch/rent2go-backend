package app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
@Embeddable
public class RatingValue extends ValueObject {

    @Column(name = "rating_value", nullable = false)
    private Integer value;

    private RatingValue(Integer value) {
        if (value == null || value < 1 || value > 5) {
            throw new IllegalArgumentException("Rating value must be between 1 and 5");
        }

        this.value = value;
    }

    public static RatingValue of(Integer value) {
        return new RatingValue(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RatingValue that = (RatingValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}