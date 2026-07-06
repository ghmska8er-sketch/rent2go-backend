package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.WithdrawalStatus;
import app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories.WithdrawalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

/**
 * WithdrawalServiceTest
 *
 * Unit tests for Phase 3's withdrawal/payout domain (US48/US49/TS12): successful withdrawal
 * within balance, rejected withdrawal exceeding balance, and available-balance derivation.
 * All IO (WithdrawalRepository, PaymentsService) is mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private WithdrawalRepository withdrawalRepository;

    @Mock
    private PaymentsService paymentsService;

    private WithdrawalService service;

    @BeforeEach
    void setUp() {
        service = new WithdrawalService(withdrawalRepository, paymentsService);
    }

    @Test
    void requestWithdrawal_succeeds_whenAmountIsWithinAvailableBalance() {
        when(paymentsService.sumSucceededAmountCentsByOwnerBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10000L); // $100.00 total earned
        when(withdrawalRepository.sumAmountCentsByOwner(1L)).thenReturn(2000L); // $20.00 already withdrawn
        when(withdrawalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var withdrawal = service.requestWithdrawal(1L, 5000L, "Yape 999999999");

        assertEquals(1L, withdrawal.getOwnerId());
        assertEquals(5000L, withdrawal.getAmountCents());
        assertEquals(WithdrawalStatus.PENDING, withdrawal.getStatus());
        assertEquals("Yape 999999999", withdrawal.getPayoutDestinationNote());
    }

    @Test
    void requestWithdrawal_staysPending_noRealPayoutRailToConfirmIt() {
        when(paymentsService.sumSucceededAmountCentsByOwnerBetween(eq(7L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10000L);
        when(withdrawalRepository.sumAmountCentsByOwner(7L)).thenReturn(0L);
        when(withdrawalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var withdrawal = service.requestWithdrawal(7L, 3000L, null);

        assertEquals(WithdrawalStatus.PENDING, withdrawal.getStatus());
    }

    @Test
    void requestWithdrawal_throws_whenAmountExceedsAvailableBalance() {
        when(paymentsService.sumSucceededAmountCentsByOwnerBetween(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3000L); // $30.00 total earned
        when(withdrawalRepository.sumAmountCentsByOwner(2L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> service.requestWithdrawal(2L, 5000L, null));
    }

    @Test
    void requestWithdrawal_throws_whenAmountIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> service.requestWithdrawal(3L, 0L, null));
        assertThrows(IllegalArgumentException.class, () -> service.requestWithdrawal(3L, -100L, null));
    }

    @Test
    void getAvailableBalanceCents_subtractsWithdrawnFromTotalEarned() {
        when(paymentsService.sumSucceededAmountCentsByOwnerBetween(eq(4L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10000L);
        when(withdrawalRepository.sumAmountCentsByOwner(4L)).thenReturn(4000L);

        assertEquals(6000L, service.getAvailableBalanceCents(4L));
    }

    @Test
    void getAvailableBalanceCents_neverGoesNegative() {
        when(paymentsService.sumSucceededAmountCentsByOwnerBetween(eq(5L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1000L);
        when(withdrawalRepository.sumAmountCentsByOwner(5L)).thenReturn(5000L);

        assertEquals(0L, service.getAvailableBalanceCents(5L));
    }

    @Test
    void getPendingWithdrawnCents_delegatesToRepositoryWithPendingStatus() {
        when(withdrawalRepository.sumAmountCentsByOwnerAndStatus(6L, WithdrawalStatus.PENDING)).thenReturn(1500L);

        assertEquals(1500L, service.getPendingWithdrawnCents(6L));
    }
}
