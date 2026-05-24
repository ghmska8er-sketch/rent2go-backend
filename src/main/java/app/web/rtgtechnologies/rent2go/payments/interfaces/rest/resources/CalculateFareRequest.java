package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class CalculateFareRequest {
    @NotNull(message = "Base amount is required")
    private BigDecimal baseAmount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @Valid
    private List<FeeDto> fees;

    @Valid
    private List<DiscountDto> discounts;
    private String promoCode;
    private String coveragePlan;

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

    public String getCoveragePlan() { return coveragePlan; }
    public void setCoveragePlan(String coveragePlan) { this.coveragePlan = coveragePlan; }
}
