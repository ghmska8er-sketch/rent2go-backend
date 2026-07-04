package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateWithdrawalRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amountCents;

    @Size(max = 500, message = "Payout destination note must be at most 500 characters")
    private String payoutDestinationNote;

    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getPayoutDestinationNote() { return payoutDestinationNote; }
    public void setPayoutDestinationNote(String payoutDestinationNote) { this.payoutDestinationNote = payoutDestinationNote; }
}
