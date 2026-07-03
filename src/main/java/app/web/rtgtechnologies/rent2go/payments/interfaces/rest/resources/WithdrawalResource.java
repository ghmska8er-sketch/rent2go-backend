package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

public class WithdrawalResource {
    private Long id;
    private Long ownerId;
    private Long amountCents;
    private String payoutDestinationNote;
    private String status;
    private String requestedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getPayoutDestinationNote() { return payoutDestinationNote; }
    public void setPayoutDestinationNote(String payoutDestinationNote) { this.payoutDestinationNote = payoutDestinationNote; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRequestedAt() { return requestedAt; }
    public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }
}
