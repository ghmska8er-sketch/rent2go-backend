package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * PricePerDay Value Object
 * 
 * Represents the daily rental price for a vehicle.
 * Ensures price is always positive and in a valid currency.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PricePerDay extends ValueObject {

    private BigDecimal amount;
    private String currency;

    public static PricePerDay USD(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        return new PricePerDay(amount, "USD");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PricePerDay that = (PricePerDay) o;
        return Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
