package app.web.rtgtechnologies.rent2go.payments.interfaces.rest;

import app.web.rtgtechnologies.rent2go.payments.application.internal.services.FareCalculationServiceImpl;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.StripePaymentService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.VehiclePerformanceService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.WithdrawalService;
import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Payment;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import com.stripe.exception.ApiConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PaymentsControllerSyncTest
 *
 * Covers the defensive POST /reservations/{id}/sync fallback: it retrieves the reservation's
 * PaymentIntent directly from Stripe (StripePaymentService.syncPaymentIntent) and, only when
 * Stripe reports "succeeded", marks the local payment record as SUCCEEDED. This is a fallback
 * for when the Stripe webhook has not reached the backend — the webhook remains the source of
 * truth for the reservation's own confirm/mark-paid transition (applied inside
 * StripePaymentService, not re-tested here).
 */
@ExtendWith(MockitoExtension.class)
class PaymentsControllerSyncTest {

    @Mock private FareCalculationServiceImpl fareCalculationService;
    @Mock private StripePaymentService stripePaymentService;
    @Mock private PaymentsService paymentsService;
    @Mock private PromoService promoService;
    @Mock private VehiclePerformanceService vehiclePerformanceService;
    @Mock private CurrentUserService currentUserService;
    @Mock private WithdrawalService withdrawalService;

    private PaymentsController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentsController(
                fareCalculationService,
                stripePaymentService,
                paymentsService,
                promoService,
                vehiclePerformanceService,
                currentUserService,
                withdrawalService
        );
    }

    @Test
    void syncPayment_returnsNotFound_whenNoPaymentRecordExistsForReservation() throws Exception {
        when(paymentsService.findByReservationId(99L)).thenReturn(Optional.empty());

        var response = controller.syncPayment(99L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void syncPayment_marksSucceeded_whenStripeReportsIntentSucceeded() throws Exception {
        Payment payment = new Payment(10L, "pi_123", 5000L, "usd", "CREATED");
        when(paymentsService.findByReservationId(10L)).thenReturn(Optional.of(payment));
        when(stripePaymentService.syncPaymentIntent("pi_123")).thenReturn(true);

        var response = controller.syncPayment(10L);

        assertEquals(200, response.getStatusCode().value());
        verify(paymentsService, times(1)).markSucceeded("pi_123");
    }

    @Test
    void syncPayment_doesNotMarkSucceeded_whenStripeIntentIsNotSucceededYet() throws Exception {
        Payment payment = new Payment(11L, "pi_456", 5000L, "usd", "CREATED");
        when(paymentsService.findByReservationId(11L)).thenReturn(Optional.of(payment));
        when(stripePaymentService.syncPaymentIntent("pi_456")).thenReturn(false);

        var response = controller.syncPayment(11L);

        assertEquals(200, response.getStatusCode().value());
        verify(paymentsService, never()).markSucceeded(anyString());
    }

    @Test
    void syncPayment_returns500_whenStripeCallFails() throws Exception {
        Payment payment = new Payment(12L, "pi_789", 5000L, "usd", "CREATED");
        when(paymentsService.findByReservationId(12L)).thenReturn(Optional.of(payment));
        when(stripePaymentService.syncPaymentIntent("pi_789"))
                .thenThrow(new ApiConnectionException("network error"));

        var response = controller.syncPayment(12L);

        assertEquals(500, response.getStatusCode().value());
        verify(paymentsService, never()).markSucceeded(anyString());
    }
}
