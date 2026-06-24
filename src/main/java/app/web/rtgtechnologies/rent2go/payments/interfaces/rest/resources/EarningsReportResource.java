package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

public class EarningsReportResource {
    private Long ownerId;
    private Long totalAmountCents;
    private java.math.BigDecimal totalAmount;
    private Long availablePayoutCents;
    private Long pendingPayoutCents;
    private String currency;
    private String from;
    private String to;
    private Long paymentsCount;
    private String nextPayoutDate;
    private Long availableNowCents;

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getTotalAmountCents() { return totalAmountCents; }
    public void setTotalAmountCents(Long totalAmountCents) { this.totalAmountCents = totalAmountCents; }
    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getAvailablePayoutCents() { return availablePayoutCents; }
    public void setAvailablePayoutCents(Long availablePayoutCents) { this.availablePayoutCents = availablePayoutCents; }
    public Long getPendingPayoutCents() { return pendingPayoutCents; }
    public void setPendingPayoutCents(Long pendingPayoutCents) { this.pendingPayoutCents = pendingPayoutCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public Long getPaymentsCount() { return paymentsCount; }
    public void setPaymentsCount(Long paymentsCount) { this.paymentsCount = paymentsCount; }
    public String getNextPayoutDate() { return nextPayoutDate; }
    public void setNextPayoutDate(String nextPayoutDate) { this.nextPayoutDate = nextPayoutDate; }
    public Long getAvailableNowCents() { return availableNowCents; }
    public void setAvailableNowCents(Long availableNowCents) { this.availableNowCents = availableNowCents; }
}
