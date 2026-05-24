package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ConfirmReturnResource {
    @NotNull(message = "Actor ID is required")
    @Positive(message = "Actor ID must be positive")
    private Long actorId;

    public ConfirmReturnResource() {}

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }
}
