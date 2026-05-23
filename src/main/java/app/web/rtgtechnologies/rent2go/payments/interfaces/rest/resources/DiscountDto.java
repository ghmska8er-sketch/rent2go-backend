package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import java.math.BigDecimal;

public class DiscountDto {
    private String code;
    private BigDecimal percentage;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
