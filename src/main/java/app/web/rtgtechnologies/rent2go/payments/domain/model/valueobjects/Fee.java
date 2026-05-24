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
public class Fee {

    @Column(name = "fee_code", length = 50)
    private String code;

    @Column(name = "fee_amount", precision = 14, scale = 2)
    private BigDecimal amount;

    public static Fee of(String code, BigDecimal amount) {
        return new Fee(code, amount == null ? BigDecimal.ZERO : amount);
    }
}
