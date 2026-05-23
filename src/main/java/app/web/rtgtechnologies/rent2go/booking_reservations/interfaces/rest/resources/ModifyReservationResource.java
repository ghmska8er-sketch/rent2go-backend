package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import java.time.LocalDate;

public record ModifyReservationResource(LocalDate startDate, LocalDate endDate, Long requestedBy) {}
