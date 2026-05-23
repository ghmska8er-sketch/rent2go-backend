package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import java.math.BigDecimal;

public class FeeDto {
    private String code;
    private BigDecimal amount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
