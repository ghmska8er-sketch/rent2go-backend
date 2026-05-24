package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for status update requests coming from REST API.
 */
public class UpdateReservationStatusResource {
    @NotNull(message = "Actor ID is required")
    @Positive(message = "Actor ID must be positive")
    private Long actorId;

    @NotBlank(message = "Status is required")
    private String status;

    public UpdateReservationStatusResource() {}

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
