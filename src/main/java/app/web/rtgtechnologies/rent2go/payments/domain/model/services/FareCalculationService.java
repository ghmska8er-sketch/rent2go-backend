package app.web.rtgtechnologies.rent2go.payments.domain.model.services;

import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Discount;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Fee;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Money;

import java.util.List;

/**
 * Port for calculating final fares given base price, fees and discounts.
 */
public interface FareCalculationService {

    /**
     * Calculate final amount using base price, optional fees and discounts.
     */
    Money calculate(Money basePrice, List<Fee> fees, List<Discount> discounts);
}
