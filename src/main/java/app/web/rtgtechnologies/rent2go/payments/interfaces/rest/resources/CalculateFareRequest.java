package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

public class CalculateFareRequest {
    private BigDecimal baseAmount;
    private String currency;
    private List<FeeDto> fees;
    private List<DiscountDto> discounts;
    private String promoCode;

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<FeeDto> getFees() {
        return fees;
    }

    public void setFees(List<FeeDto> fees) {
        this.fees = fees;
    }

    public List<DiscountDto> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<DiscountDto> discounts) {
        this.discounts = discounts;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }
}
