package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreatePromoRequest {
    @NotBlank(message = "Promo code is required")
    private String code;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Percentage must be greater than 0")
    private BigDecimal percentage;

    private String expiresAt; // ISO-8601 string, optional

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
