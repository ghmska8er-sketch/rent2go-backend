package app.web.rtgtechnologies.rent2go.payments.interfaces.rest;

import app.web.rtgtechnologies.rent2go.payments.application.internal.services.FareCalculationServiceImpl;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.StripePaymentService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.VehiclePerformanceService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.WithdrawalService;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * PaymentsControllerEarningsOwnershipTest
 *
 * Phase 1 security fix: GET /payments/owners/{ownerId}/earnings must reject any authenticated
 * user who is neither the requested owner nor an admin. CurrentUserService is mocked directly
 * so this test exercises only the controller's authorization branch, not JWT/security-context
 * plumbing (covered separately by CurrentUserServiceTest).
 */
@ExtendWith(MockitoExtension.class)
class PaymentsControllerEarningsOwnershipTest {

    @Mock
    private FareCalculationServiceImpl fareCalculationService;
    @Mock
    private StripePaymentService stripePaymentService;
    @Mock
    private PaymentsService paymentsService;
    @Mock
    private PromoService promoService;
    @Mock
    private VehiclePerformanceService vehiclePerformanceService;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private WithdrawalService withdrawalService;

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
    void getOwnerEarnings_returnsOk_whenCallerIsRealOwner() {
        when(currentUserService.isOwnerOrAdmin(7L)).thenReturn(true);
        when(paymentsService.sumSucceededAmountCentsByOwnerBetween(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(10000L);
        when(paymentsService.countSucceededPaymentsByOwnerBetween(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(3L);
        when(withdrawalService.getAvailableBalanceCents(7L)).thenReturn(10000L);
        when(withdrawalService.getPendingWithdrawnCents(7L)).thenReturn(0L);

        var response = controller.getOwnerEarnings(7L, "2026-01-01", "2026-01-31");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getOwnerEarnings_returnsForbidden_whenCallerIsDifferentUser() {
        when(currentUserService.isOwnerOrAdmin(7L)).thenReturn(false);

        var response = controller.getOwnerEarnings(7L, "2026-01-01", "2026-01-31");

        assertEquals(403, response.getStatusCode().value());
    }
}
