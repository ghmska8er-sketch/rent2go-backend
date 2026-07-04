package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.CreateReservationCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.NotificationService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.VehicleAvailabilityQueryService;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.BookingStatus;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.UserReputationRepository;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TS20 consistency fix — the write-path overlap check in ReservationCommandServiceImpl must
 * reject a new reservation overlapping an existing one in RETURN_PENDING/RETURN_CONFIRMED, not
 * just PENDING/CONFIRMED/ACTIVE, so it stays consistent with the new search-filter read path.
 */
@ExtendWith(MockitoExtension.class)
class ReservationCommandServiceImplOverlapTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private VehicleAvailabilityQueryService availabilityQueryService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private UserReputationRepository userReputationRepository;

    @InjectMocks
    private ReservationCommandServiceImpl service;

    @ParameterizedTest
    @ValueSource(strings = {"RETURN_PENDING", "RETURN_CONFIRMED"})
    void rejectsOverlapWithReturnStatuses(String blockingStatus) {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getOwnerId()).thenReturn(2L);
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(userReputationRepository.findByUserId(1L)).thenReturn(Optional.empty());

        Reservation existing = mock(Reservation.class);
        when(existing.getStatus()).thenReturn(new BookingStatus(blockingStatus));
        when(existing.getDateRange()).thenReturn(DateRange.of(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7)));
        when(reservationRepository.findAllByVehicleId(10L)).thenReturn(List.of(existing));

        var command = new CreateReservationCommand(
            10L, 1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5),
            BigDecimal.valueOf(100), null, null, null, null, null
        );

        assertThrows(IllegalStateException.class, () -> service.handle(command));
    }
}
