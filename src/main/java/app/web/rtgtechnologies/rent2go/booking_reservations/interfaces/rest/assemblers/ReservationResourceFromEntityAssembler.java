package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ReservationResource;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.CounterpartyResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TS18 — extended to embed a nested counterparty (renter/owner) object, mirroring the
 * community_trust -> iam cross-context read pattern already established in
 * ReviewCommandServiceImpl. Additive only: renterId/ownerId fields are unchanged.
 */
@Component
@RequiredArgsConstructor
public class ReservationResourceFromEntityAssembler {

    private static final String NO_NAME_ON_FILE = "Usuario sin nombre registrado";

    private final UserRepository userRepository;

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
            r.getPickupLocation(),
            r.getReturnLocation(),
            r.getCoveragePlan(),
            r.getPickupPhotos(),
            r.getReturnPhotos(),
            r.getDamageReport(),
            toCounterparty(r.getRenterId()),
            toCounterparty(r.getOwnerId())
        );
    }

    private CounterpartyResource toCounterparty(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
            .map(this::toCounterpartyResource)
            .orElse(new CounterpartyResource(userId, NO_NAME_ON_FILE, false));
    }

    private CounterpartyResource toCounterpartyResource(User user) {
        String fullName = user.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = NO_NAME_ON_FILE;
        }
        return new CounterpartyResource(user.getId(), fullName, user.isKycVerified());
    }
}
