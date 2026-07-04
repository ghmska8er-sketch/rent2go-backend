package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.VehicleAvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TS20 — verifies the bulk "blocked vehicle IDs for a date range" read query used by
 * availability-aware search. Excludes vehicles with an overlapping VehicleAvailability block or
 * an overlapping Reservation in a blocking status (PENDING/CONFIRMED/ACTIVE/RETURN_PENDING/
 * RETURN_CONFIRMED, per the user's confirmed decision to include the two return states).
 */
@ExtendWith(MockitoExtension.class)
class VehicleAvailabilityQueryServiceImplTest {

    @Mock
    private VehicleAvailabilityRepository availabilityRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private VehicleAvailabilityQueryServiceImpl service;

    private static final LocalDate SEARCH_START = LocalDate.of(2026, 8, 1);
    private static final LocalDate SEARCH_END = LocalDate.of(2026, 8, 5);

    @Test
    void excludesVehicleWithOverlappingConfirmedReservation() {
        Reservation confirmed = reservation(42L, "CONFIRMED", LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7));
        when(availabilityRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.findAllInBlockingStatus()).thenReturn(List.of(confirmed));

        Set<Long> blocked = service.findBlockedVehicleIds(SEARCH_START, SEARCH_END);

        assertTrue(blocked.contains(42L));
    }

    @Test
    void doesNotExcludeVehicleWithOnlyCancelledReservation() {
        // CANCELLED is not part of findAllInBlockingStatus's result set, so a cancelled-only
        // reservation for vehicle 7 would simply never appear here — simulating that directly.
        when(availabilityRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.findAllInBlockingStatus()).thenReturn(List.of());

        Set<Long> blocked = service.findBlockedVehicleIds(SEARCH_START, SEARCH_END);

        assertFalse(blocked.contains(7L));
        assertTrue(blocked.isEmpty());
    }

    @Test
    void excludesVehicleWithOverlappingReturnPendingReservation() {
        Reservation returnPending = reservation(99L, "RETURN_PENDING", LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 2));
        when(availabilityRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.findAllInBlockingStatus()).thenReturn(List.of(returnPending));

        Set<Long> blocked = service.findBlockedVehicleIds(SEARCH_START, SEARCH_END);

        assertTrue(blocked.contains(99L));
    }

    @Test
    void excludesVehicleWithOverlappingAvailabilityBlock() {
        VehicleAvailability block = mock(VehicleAvailability.class);
        when(block.getVehicleId()).thenReturn(13L);
        when(block.overlaps(DateRange.of(SEARCH_START, SEARCH_END))).thenReturn(true);
        when(availabilityRepository.findAll()).thenReturn(List.of(block));
        when(reservationRepository.findAllInBlockingStatus()).thenReturn(List.of());

        Set<Long> blocked = service.findBlockedVehicleIds(SEARCH_START, SEARCH_END);

        assertTrue(blocked.contains(13L));
    }

    @Test
    void returnsEmptySetWhenNoDateRangeProvided() {
        Set<Long> blocked = service.findBlockedVehicleIds(null, null);

        assertEquals(Set.of(), blocked);
    }

    /**
     * Builds a mock reservation as returned by ReservationRepository.findAllInBlockingStatus()
     * — status filtering already happened at the repository query level (see that method's
     * @Query), so the status string here is documentation of test intent only, not stubbed on
     * the mock (the production code under test never calls getStatus() on these results).
     */
    private Reservation reservation(Long vehicleId, String status, LocalDate start, LocalDate end) {
        Reservation r = mock(Reservation.class);
        when(r.getVehicleId()).thenReturn(vehicleId);
        when(r.getDateRange()).thenReturn(DateRange.of(start, end));
        return r;
    }
}
