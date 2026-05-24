package app.web.rtgtechnologies.rent2go.community_trust.interfaces.rest.resources;

import java.math.BigDecimal;

public record VehicleRatingResource(
    Long vehicleId,
    BigDecimal averageRating,
    Integer reviewCount
) {
}