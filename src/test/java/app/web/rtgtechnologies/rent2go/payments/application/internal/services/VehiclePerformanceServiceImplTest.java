package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.BookingStatus;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.VehiclePerformanceResource;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * VehiclePerformanceServiceImplTest
 *
 * Unit tests for US24 per-vehicle performance calculation.
 * All IO (repositories) is mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class VehiclePerformanceServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentsService paymentsService;

    private VehiclePerformanceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VehiclePerformanceServiceImpl(vehicleRepository, reservationRepository, paymentsService);
    }

    @Test
    void getPerformance_throwsIllegalArgumentException_whenVehicleDoesNotExist() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getPerformance(999L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));
    }

    @Test
    void getPerformance_returnsAllZeros_whenVehicleHasNoReservationHistory() {
        Vehicle vehicle = mockVehicle(1L, Date.from(LocalDate.of(2026, 1, 1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservationRepository.findAllByVehicleId(1L)).thenReturn(List.of());
        when(paymentsService.sumSucceededAmountCentsByVehicleBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);

        VehiclePerformanceResource result = service.getPerformance(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(0, result.getReservationCount());
        assertEquals(BigDecimal.valueOf(0, 2), result.getTotalRevenue());
        assertEquals(0.0, result.getOccupancyPercentage());
    }

    @Test
    void getPerformance_sumsRevenueFromSucceededPayments() {
        Vehicle vehicle = mockVehicle(2L, Date.from(LocalDate.of(2026, 1, 1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));
        when(reservationRepository.findAllByVehicleId(2L)).thenReturn(List.of());
        when(paymentsService.sumSucceededAmountCentsByVehicleBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(15000L); // $150.00

        VehiclePerformanceResource result = service.getPerformance(2L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(new BigDecimal("150.00"), result.getTotalRevenue());
        assertEquals("PEN", result.getCurrency());
    }

    @Test
    void getPerformance_calculatesOccupancyFromBookedDaysOverWindow() {
        // 31-day period (Jan 1 - Jan 31), a single 10-day (inclusive) reservation -> 10/31 * 100
        Vehicle vehicle = mockVehicle(3L, Date.from(LocalDate.of(2026, 1, 1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicle));

        Reservation reservation = mockReservation(
                DateRange.of(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 14)), // 10 days inclusive
                "CONFIRMED"
        );
        when(reservationRepository.findAllByVehicleId(3L)).thenReturn(List.of(reservation));
        when(paymentsService.sumSucceededAmountCentsByVehicleBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        VehiclePerformanceResource result = service.getPerformance(3L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        double expected = (10.0 / 31.0) * 100.0;
        assertEquals(expected, result.getOccupancyPercentage(), 0.001);
        assertEquals(1, result.getReservationCount());
    }

    @Test
    void getPerformance_excludesCancelledReservationsFromOccupancy() {
        Vehicle vehicle = mockVehicle(4L, Date.from(LocalDate.of(2026, 1, 1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        when(vehicleRepository.findById(4L)).thenReturn(Optional.of(vehicle));

        Reservation cancelled = mockReservation(
                DateRange.of(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 14)),
                "CANCELLED"
        );
        when(reservationRepository.findAllByVehicleId(4L)).thenReturn(List.of(cancelled));
        when(paymentsService.sumSucceededAmountCentsByVehicleBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        VehiclePerformanceResource result = service.getPerformance(4L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(0.0, result.getOccupancyPercentage());
    }

    private Vehicle mockVehicle(Long id, Date createdAt) {
        Vehicle vehicle = org.mockito.Mockito.mock(Vehicle.class);
        when(vehicle.getCreatedAt()).thenReturn(createdAt);
        return vehicle;
    }

    private Reservation mockReservation(DateRange dateRange, String status) {
        Reservation reservation = org.mockito.Mockito.mock(Reservation.class);
        when(reservation.getDateRange()).thenReturn(dateRange);
        BookingStatus bookingStatus = org.mockito.Mockito.mock(BookingStatus.class);
        when(bookingStatus.getStatus()).thenReturn(status);
        when(reservation.getStatus()).thenReturn(bookingStatus);
        return reservation;
    }
}
