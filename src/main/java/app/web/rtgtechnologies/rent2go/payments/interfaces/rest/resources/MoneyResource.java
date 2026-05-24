package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import java.math.BigDecimal;

public class MoneyResource {
    private BigDecimal amount;
    private String currency;
    private BigDecimal subtotal;
    private BigDecimal serviceFee;
    private BigDecimal coverageFee;
    private BigDecimal discount;
    private BigDecimal taxes;
    private BigDecimal total;

    public MoneyResource() {}

    public MoneyResource(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
        this.total = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getCoverageFee() {
        return coverageFee;
    }

    public void setCoverageFee(BigDecimal coverageFee) {
        this.coverageFee = coverageFee;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
        this.amount = total;
    }
}
