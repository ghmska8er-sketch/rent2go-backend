package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources;

import java.util.Date;

public record FavoriteResource(Long id, Long renterId, Long vehicleId, Date createdAt) {}
