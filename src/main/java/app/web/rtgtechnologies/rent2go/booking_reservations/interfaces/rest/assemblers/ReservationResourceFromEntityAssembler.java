package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ReservationResource;
import org.springframework.stereotype.Component;

@Component
public class ReservationResourceFromEntityAssembler {

    public ReservationResource toResource(Reservation r) {
        return new ReservationResource(
            r.getId(),
            r.getReservationCode().getCode(),
            r.getVehicleId(),
            r.getRenterId(),
            r.getOwnerId(),
            r.getDateRange().getStartDate(),
            r.getDateRange().getEndDate(),
            r.getTotalAmount(),
            r.getStatus().getStatus(),
            r.getPickupConfirmedAt(),
            r.getReturnConfirmedAt(),
            r.getDamageReport()
        );
    }
}
