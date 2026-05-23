package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

/**
 * DTO for status update requests coming from REST API.
 */
public class UpdateReservationStatusResource {
    private Long actorId;
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
