package app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    @Column(name = "discount_code", length = 50)
    private String code;

    // percentage represented as 0.10 for 10%
    @Column(name = "discount_percentage", precision = 5, scale = 4)
    private BigDecimal percentage;

    public static Discount of(String code, BigDecimal percentage) {
        if (percentage == null) percentage = BigDecimal.ZERO;
        return new Discount(code, percentage);
    }
}
