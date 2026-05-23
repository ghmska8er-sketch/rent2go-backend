package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import java.math.BigDecimal;

public class CreatePromoRequest {
    private String code;
    private BigDecimal percentage;
    private String expiresAt; // ISO-8601 string, optional

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
