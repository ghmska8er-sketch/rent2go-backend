package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.payments.domain.model.services.FareCalculationService;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Discount;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Fee;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FareCalculationServiceImpl implements FareCalculationService {

    @Override
    public Money calculate(Money basePrice, List<Fee> fees, List<Discount> discounts) {
        if (basePrice == null) throw new IllegalArgumentException("Base price required");

        BigDecimal result = basePrice.getAmount();

        if (fees != null) {
            for (Fee f : fees) {
                if (f != null && f.getAmount() != null) result = result.add(f.getAmount());
            }
        }

        if (discounts != null) {
            for (Discount d : discounts) {
                if (d != null && d.getPercentage() != null) {
                    BigDecimal discountAmount = result.multiply(d.getPercentage());
                    result = result.subtract(discountAmount);
                }
            }
        }

        return Money.of(result, basePrice.getCurrency());
    }
}
