package app.web.rtgtechnologies.rent2go.payments.interfaces.rest;

import app.web.rtgtechnologies.rent2go.payments.application.internal.services.FareCalculationServiceImpl;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.StripePaymentService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.VehiclePerformanceService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.WithdrawalService;
import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Withdrawal;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateWithdrawalRequest;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * PaymentsControllerWithdrawalTest
 *
 * Phase 3: POST/GET .../withdrawals must be ownership-checked (403 for a non-owner caller),
 * return 201 on a successful request, 400 on insufficient balance, and the history endpoint
 * must return only the requesting owner's records (delegated to WithdrawalService, mocked
 * here to isolate the controller's transport/authorization behavior).
 */
@ExtendWith(MockitoExtension.class)
class PaymentsControllerWithdrawalTest {

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
    void requestWithdrawal_returnsCreated_whenWithinBalance() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(true);
        Withdrawal withdrawal = new Withdrawal(1L, 5000L, "Yape 999999999");
        when(withdrawalService.requestWithdrawal(1L, 5000L, "Yape 999999999")).thenReturn(withdrawal);

        var request = new CreateWithdrawalRequest();
        request.setAmountCents(5000L);
        request.setPayoutDestinationNote("Yape 999999999");

        var response = controller.requestWithdrawal(1L, request);

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void requestWithdrawal_returnsBadRequest_whenAmountExceedsBalance() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(true);
        when(withdrawalService.requestWithdrawal(1L, 999999L, null))
                .thenThrow(new IllegalArgumentException("Requested amount exceeds available balance"));

        var request = new CreateWithdrawalRequest();
        request.setAmountCents(999999L);

        var response = controller.requestWithdrawal(1L, request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void requestWithdrawal_returnsForbidden_whenCallerIsNotOwner() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(false);

        var request = new CreateWithdrawalRequest();
        request.setAmountCents(1000L);

        var response = controller.requestWithdrawal(1L, request);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getWithdrawalHistory_returnsOnlyRequestingOwnersRecords() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(true);
        Withdrawal withdrawal = new Withdrawal(1L, 5000L, null);
        Page<Withdrawal> page = new PageImpl<>(List.of(withdrawal));
        when(withdrawalService.getWithdrawalHistory(1L, 0, 20)).thenReturn(page);

        var response = controller.getWithdrawalHistory(1L, 1, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().content().size());
        assertEquals(1L, response.getBody().content().get(0).getOwnerId());
    }

    @Test
    void getWithdrawalHistory_returnsForbidden_whenCallerIsNotOwner() {
        when(currentUserService.isOwnerOrAdmin(1L)).thenReturn(false);

        var response = controller.getWithdrawalHistory(1L, 1, 20);

        assertEquals(403, response.getStatusCode().value());
    }
}
