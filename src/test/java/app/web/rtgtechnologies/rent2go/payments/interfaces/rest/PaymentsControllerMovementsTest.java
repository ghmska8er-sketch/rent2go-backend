package app.web.rtgtechnologies.rent2go.payments.interfaces.rest;

import app.web.rtgtechnologies.rent2go.payments.application.internal.services.FareCalculationServiceImpl;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.StripePaymentService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.VehiclePerformanceService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.WithdrawalService;
import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Payment;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * PaymentsControllerMovementsTest
 *
 * Phase 4 (US47/TS12): GET .../earnings/movements must be ownership-checked (403 for a
 * non-owner caller) and return itemized payment records for the real owner.
 */
@ExtendWith(MockitoExtension.class)
class PaymentsControllerMovementsTest {

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
    void getOwnerEarningsMovements_returnsOk_whenCallerIsRealOwner() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(true);
        Payment payment = new Payment(10L, "pi_123", 5000L, "USD", "SUCCEEDED");
        when(paymentsService.findAllByOwnerBetween(eq(1L), any(), any())).thenReturn(List.of(payment));

        var response = controller.getOwnerEarningsMovements(1L, "2026-01-01", "2026-01-31");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).getReservationId());
    }

    @Test
    void getOwnerEarningsMovements_returnsForbidden_whenCallerIsNotOwner() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(false);

        var response = controller.getOwnerEarningsMovements(1L, "2026-01-01", "2026-01-31");

        assertEquals(403, response.getStatusCode().value());
    }
}
