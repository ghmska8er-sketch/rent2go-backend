package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import java.math.BigDecimal;

public class PromoCodeResource {
    private String code;
    private BigDecimal percentage;
    private boolean active;
    private String expiresAt;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
