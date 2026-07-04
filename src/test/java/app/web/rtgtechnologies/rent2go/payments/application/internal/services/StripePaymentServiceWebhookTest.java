package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.VehicleAvailabilityRepository;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the payment-succeeded transition shared by the Stripe webhook handler
 * (handleWebhookEvent -> handlePaymentSucceeded -> applyPaymentSucceeded) and the defensive
 * syncPaymentIntent fallback. Prior to this test, StripePaymentService had zero test coverage,
 * so the fact that a successful PaymentIntent actually flips Reservation.status from PENDING to
 * CONFIRMED (via Reservation.markPaid -> confirm()) was unverified — this closes that gap.
 *
 * applyPaymentSucceeded/handlePaymentSucceeded are private, so the shared logic is exercised
 * through the public syncPaymentIntent(String) entry point, which delegates to the exact same
 * method the webhook uses.
 */
@ExtendWith(MockitoExtension.class)
class StripePaymentServiceWebhookTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleAvailabilityRepository availabilityRepository;

    @InjectMocks
    private StripePaymentService stripePaymentService;

    private Reservation newPendingReservation(Long vehicleId, Long renterId, Long ownerId) {
        return Reservation.create(
                vehicleId,
                renterId,
                ownerId,
                DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)),
                BigDecimal.valueOf(150));
    }

    @Test
    void applyPaymentSucceeded_transitionsPendingReservationToConfirmed() throws Exception {
        Reservation reservation = newPendingReservation(1L, 2L, 3L);
        assertTrue(reservation.getStatus().isPending(), "sanity check: reservation starts PENDING");

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(availabilityRepository.findAllByVehicleId(1L)).thenReturn(List.of());

        PaymentIntent intent = mockSucceededIntent("pi_test_123", 10L);

        try (MockedStatic<PaymentIntent> piStatic = mockStatic(PaymentIntent.class)) {
            piStatic.when(() -> PaymentIntent.retrieve("pi_test_123")).thenReturn(intent);

            boolean applied = stripePaymentService.syncPaymentIntent("pi_test_123");

            assertTrue(applied, "syncPaymentIntent should report the intent as applied");
        }

        // The core assertion this test exists for: the shared payment-succeeded logic must
        // actually mutate Reservation.status, not just a separate payment record.
        assertEquals("CONFIRMED", reservation.getStatus().getStatus());
        assertNotNull(reservation.getPaidAt());
        assertEquals("pi_test_123", reservation.getPaymentIntentId());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void applyPaymentSucceeded_blocksVehicleAvailabilityForReservationDates() throws Exception {
        Reservation reservation = newPendingReservation(5L, 2L, 3L);
        when(reservationRepository.findById(20L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(availabilityRepository.findAllByVehicleId(5L)).thenReturn(List.of());

        PaymentIntent intent = mockSucceededIntent("pi_test_456", 20L);

        try (MockedStatic<PaymentIntent> piStatic = mockStatic(PaymentIntent.class)) {
            piStatic.when(() -> PaymentIntent.retrieve("pi_test_456")).thenReturn(intent);
            stripePaymentService.syncPaymentIntent("pi_test_456");
        }

        verify(availabilityRepository, times(1)).save(any());
    }

    @Test
    void applyPaymentSucceeded_isIdempotent_whenReservationAlreadyConfirmed() throws Exception {
        Reservation reservation = newPendingReservation(7L, 2L, 3L);
        reservation.confirm();
        assertTrue(reservation.getStatus().isConfirmed(), "sanity check: reservation is already CONFIRMED");

        when(reservationRepository.findById(30L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(availabilityRepository.findAllByVehicleId(7L)).thenReturn(List.of());

        PaymentIntent intent = mockSucceededIntent("pi_test_789", 30L);

        try (MockedStatic<PaymentIntent> piStatic = mockStatic(PaymentIntent.class)) {
            piStatic.when(() -> PaymentIntent.retrieve("pi_test_789")).thenReturn(intent);
            stripePaymentService.syncPaymentIntent("pi_test_789");
        }

        // Re-applying a succeeded event to an already-confirmed reservation must not throw
        // (Reservation.confirm() would raise IllegalStateException if called again) and must
        // not overwrite the paymentIntentId that was never set by this second event.
        assertEquals("CONFIRMED", reservation.getStatus().getStatus());
    }

    @Test
    void applyPaymentSucceeded_doesNothing_whenReservationIdMissingFromMetadata() throws Exception {
        PaymentIntent intent = new PaymentIntent();
        intent.setId("pi_no_metadata");
        intent.setStatus("succeeded");
        intent.setMetadata(new HashMap<>());

        try (MockedStatic<PaymentIntent> piStatic = mockStatic(PaymentIntent.class)) {
            piStatic.when(() -> PaymentIntent.retrieve("pi_no_metadata")).thenReturn(intent);
            stripePaymentService.syncPaymentIntent("pi_no_metadata");
        }

        verify(reservationRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }

    private PaymentIntent mockSucceededIntent(String id, Long reservationId) {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(id);
        intent.setStatus("succeeded");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", String.valueOf(reservationId));
        intent.setMetadata(metadata);
        return intent;
    }
}
