package app.web.rtgtechnologies.rent2go.payments.interfaces.rest;

import app.web.rtgtechnologies.rent2go.payments.application.internal.services.FareCalculationServiceImpl;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.StripePaymentService;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Discount;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Fee;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Money;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CalculateFareRequest;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.DiscountDto;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.EarningsReportResource;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.FeeDto;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.MoneyResource;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.VehiclePerformanceResource;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.VehiclePerformanceService;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.WithdrawalService;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateWithdrawalRequest;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.WithdrawalResource;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.EarningsMovementResource;
import app.web.rtgtechnologies.rent2go.shared.application.internal.services.CurrentUserService;
import app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resource.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import com.stripe.exception.StripeException;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateIntentRequest;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateIntentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;


@Tag(name = "Payments", description = "Fare calculation, payment intents, refunds and earnings reports")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentsController {

    private static final Logger log = LoggerFactory.getLogger(PaymentsController.class);

    private final FareCalculationServiceImpl fareCalculationService;
    private final StripePaymentService stripePaymentService;
    private final app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService paymentsService;
    private final app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService promoService;
    private final VehiclePerformanceService vehiclePerformanceService;
    private final CurrentUserService currentUserService;
    private final WithdrawalService withdrawalService;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @GetMapping("/coverage-plans")
    @Operation(summary = "List coverage plans",
               description = "Returns all available coverage plans with their codes, descriptions, and daily rates. " +
                             "Pass the plan's `code` as the `coveragePlan` field when calculating a fare or creating a reservation.")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getCoveragePlans() {
        var plans = java.util.List.of(
            coveragePlan("BASIC", "Basic Coverage",
                "Covers liability and minor damage. Deductible: $500.", BigDecimal.valueOf(5.00)),
            coveragePlan("STANDARD", "Standard Coverage",
                "Covers liability, collision, and theft with a $250 deductible.", BigDecimal.valueOf(12.00)),
            coveragePlan("PREMIUM", "Premium Coverage",
                "Full coverage with zero deductible, including roadside assistance.", BigDecimal.valueOf(20.00)),
            coveragePlan("NONE", "No Coverage",
                "No additional coverage. Renter assumes all liability.", BigDecimal.ZERO)
        );
        return ResponseEntity.ok(plans);
    }

    private java.util.Map<String, Object> coveragePlan(String code, String name, String description, BigDecimal dailyRate) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("code", code);
        m.put("name", name);
        m.put("description", description);
        m.put("dailyRateUSD", dailyRate);
        return m;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate fare", description = "Calculates the trip total by applying fees, discounts and promo codes. " +
               "Use the `coveragePlan` field with a code from GET /coverage-plans (BASIC, STANDARD, PREMIUM, or NONE).")
    public ResponseEntity<MoneyResource> calculateFare(@Valid @RequestBody CalculateFareRequest request) {
        Money base = Money.of(request.getBaseAmount(), request.getCurrency());

        BigDecimal subtotal = request.getBaseAmount();

        List<Fee> fees = new java.util.ArrayList<>(
            request.getFees() == null ? List.of() : request.getFees().stream()
                .map(f -> Fee.of(f.getCode(), f.getAmount()))
                .collect(Collectors.toList()));

        // Auto-inject coverage fee from plan code if provided and no coverage fee already present
        if (request.getCoveragePlan() != null && !request.getCoveragePlan().isBlank()
                && !"NONE".equalsIgnoreCase(request.getCoveragePlan())) {
            boolean hasCoverage = fees.stream().anyMatch(f -> f.getCode() != null
                    && (f.getCode().toLowerCase().contains("coverage") || f.getCode().toLowerCase().contains("insurance")));
            if (!hasCoverage) {
                BigDecimal rate = switch (request.getCoveragePlan().toUpperCase()) {
                    case "BASIC"    -> BigDecimal.valueOf(5.00);
                    case "STANDARD" -> BigDecimal.valueOf(12.00);
                    case "PREMIUM"  -> BigDecimal.valueOf(20.00);
                    default         -> BigDecimal.ZERO;
                };
                if (rate.compareTo(BigDecimal.ZERO) > 0) {
                    fees.add(Fee.of("coverage_" + request.getCoveragePlan().toLowerCase(), rate));
                }
            }
        }

        List<Discount> discounts = request.getDiscounts() == null ? List.of() : request.getDiscounts().stream()
            .map(d -> Discount.of(d.getCode(), d.getPercentage()))
            .collect(Collectors.toList());

        // Apply promo code if present
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            var promoOpt = promoService.findActiveDiscountByCode(request.getPromoCode());
            if (promoOpt.isPresent()) {
            var promo = promoOpt.get();
            // merge into discounts list
            discounts = new java.util.ArrayList<>(discounts);
            discounts.add(promo);
            }
        }

        Money result = fareCalculationService.calculate(base, fees, discounts);
        BigDecimal serviceFee = BigDecimal.ZERO;
        BigDecimal coverageFee = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;

        for (Fee fee : fees) {
            if (fee == null || fee.getAmount() == null) {
                continue;
            }

            String code = fee.getCode() == null ? "" : fee.getCode().toLowerCase();
            if (code.contains("coverage") || code.contains("insurance")) {
                coverageFee = coverageFee.add(fee.getAmount());
            } else if (code.contains("tax")) {
                taxes = taxes.add(fee.getAmount());
            } else {
                serviceFee = serviceFee.add(fee.getAmount());
            }
        }

        BigDecimal totalBeforeDiscount = subtotal.add(serviceFee).add(coverageFee).add(taxes);
        BigDecimal discountAmount = totalBeforeDiscount.subtract(result.getAmount());

        MoneyResource res = new MoneyResource(result.getAmount(), result.getCurrency());
        res.setSubtotal(subtotal);
        res.setServiceFee(serviceFee);
        res.setCoverageFee(coverageFee);
        res.setTaxes(taxes);
        res.setDiscount(discountAmount.max(BigDecimal.ZERO));
        res.setTotal(result.getAmount());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create payment intent", description = "Creates a Stripe payment intent for a reservation and stores the payment record.")
    public ResponseEntity<CreateIntentResponse> createIntent(@Valid @RequestBody CreateIntentRequest request) {
        log.info("POST create-intent: reservationId={}, amountCents={}, currency={}",
                request.getReservationId(), request.getAmountCents(), request.getCurrency());
        try {
            var map = stripePaymentService.createPaymentIntent(request.getReservationId(), request.getAmountCents(), request.getCurrency());
            // persist payment record
            paymentsService.createRecord(request.getReservationId(), (String) map.get("id"), request.getAmountCents(), request.getCurrency());
            return ResponseEntity.ok(new CreateIntentResponse((String) map.get("clientSecret"), (String) map.get("id")));
        } catch (StripeException e) {
            log.error("Stripe rejected create-intent for reservationId={}: {}", request.getReservationId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Stripe webhook", description = "Receives Stripe events and synchronizes payment status in the backend.")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            var event = stripePaymentService.constructEvent(payload, sigHeader);
            // handleWebhookEvent now owns applying ALL payment_intent.succeeded side effects
            // (both confirming the Reservation and marking the Payment record SUCCEEDED) from a
            // single, safely-deserialized PaymentIntent — see StripePaymentService.applyPaymentSucceeded.
            // Previously this method independently re-deserialized the event here via
            // getObject().orElse(null) just to update Payment.status; that second deserialization
            // could silently return empty on a Stripe API-version mismatch even when the first
            // deserialization (inside handleWebhookEvent) succeeded, leaving Payment.status stuck
            // at CREATED despite the Reservation being confirmed correctly.
            stripePaymentService.handleWebhookEvent(event);
            return ResponseEntity.ok("received");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid payload");
        }
    }

    @PostMapping("/reservations/{reservationId}/sync")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Sync payment status with Stripe",
               description = "Defensive fallback for when the Stripe webhook has not (yet) reached the backend " +
                             "(e.g. webhook not registered in the Stripe Dashboard, or misconfigured signing secret " +
                             "in this environment). Retrieves the reservation's PaymentIntent directly from Stripe " +
                             "and, if it reports 'succeeded', applies the same confirm/mark-paid logic the webhook " +
                             "would have applied. The webhook remains the source of truth; this only unblocks a " +
                             "reservation stuck in PENDING after a genuinely successful charge.")
    public ResponseEntity<app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.PaymentReceiptResource> syncPayment(
            @PathVariable Long reservationId) {
        var opt = paymentsService.findByReservationId(reservationId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var payment = opt.get();
        try {
            boolean succeeded = stripePaymentService.syncPaymentIntent(payment.getPaymentIntentId());
            if (succeeded) {
                paymentsService.markSucceeded(payment.getPaymentIntentId());
            }
        } catch (StripeException e) {
            log.error("Stripe rejected sync for reservationId={}, paymentIntentId={}: {}",
                    reservationId, payment.getPaymentIntentId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Return the up-to-date receipt (re-read after the potential sync above)
        var refreshed = paymentsService.findByReservationId(reservationId).orElse(payment);
        var r = new app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.PaymentReceiptResource();
        r.setPaymentIntentId(refreshed.getPaymentIntentId());
        r.setAmountCents(refreshed.getAmountCents());
        r.setCurrency(refreshed.getCurrency());
        r.setStatus(refreshed.getStatus());
        r.setCreatedAt(refreshed.getCreatedAt());
        r.setUpdatedAt(refreshed.getUpdatedAt());
        return ResponseEntity.ok(r);
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Refund payment", description = "Requests a refund for a reservation payment and updates the payment status.")
    public ResponseEntity<String> refund(@Valid @RequestBody app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateIntentRequest request) {
        try {
            // use paymentIntent id from request.reservationId or amountCents as needed; here we expect amountCents and reservationId
            // Find payment record by reservation
            var opt = paymentsService.findByReservationId(request.getReservationId());
            if (opt.isEmpty()) return ResponseEntity.notFound().build();
            var payment = opt.get();
            var refundId = stripePaymentService.refundPayment(payment.getPaymentIntentId(), request.getAmountCents());
            paymentsService.markRefunded(payment.getPaymentIntentId());
            return ResponseEntity.ok(refundId);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @GetMapping("/reservations/{reservationId}/receipt")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get payment receipt", description = "Returns the receipt and status for a reservation payment.")
    public ResponseEntity<app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.PaymentReceiptResource> getReceipt(@PathVariable Long reservationId) {
        var opt = paymentsService.findByReservationId(reservationId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var p = opt.get();
        var r = new app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.PaymentReceiptResource();
        r.setPaymentIntentId(p.getPaymentIntentId());
        r.setAmountCents(p.getAmountCents());
        r.setCurrency(p.getCurrency());
        r.setStatus(p.getStatus());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/owners/{ownerId}/earnings")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get owner earnings", description = "Returns earnings and payout totals for an owner within a date range.")
    public ResponseEntity<EarningsReportResource> getOwnerEarnings(
            @PathVariable @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam @NotBlank(message = "From date is required") String from,
            @RequestParam @NotBlank(message = "To date is required") String to) {
        if (!currentUserService.isOwnerOrAdmin(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            var fromDate = LocalDate.parse(from).atStartOfDay();
            var toDate = LocalDate.parse(to).atTime(23, 59, 59);
            if (fromDate.isAfter(toDate)) {
                return ResponseEntity.badRequest().build();
            }

            var totalCents = paymentsService.sumSucceededAmountCentsByOwnerBetween(ownerId, fromDate, toDate);
            var paymentCount = paymentsService.countSucceededPaymentsByOwnerBetween(ownerId, fromDate, toDate);
            var resource = new EarningsReportResource();
            resource.setOwnerId(ownerId);
            resource.setFrom(from);
            resource.setTo(to);
            resource.setCurrency("USD");
            long cents = totalCents != null ? totalCents : 0L;
            resource.setTotalAmountCents(cents);
            resource.setTotalAmount(BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));

            // Phase 3: availablePayoutCents/pendingPayoutCents are now derived from the real
            // withdrawal ledger instead of hardcoded placeholders. Note: these two fields are
            // owner-lifetime derived values (not scoped to the from/to range), matching the
            // withdrawal balance calculation used by POST/GET .../withdrawals.
            long availableCents = withdrawalService.getAvailableBalanceCents(ownerId);
            long pendingCents = withdrawalService.getPendingWithdrawnCents(ownerId);
            resource.setAvailablePayoutCents(availableCents);
            resource.setPendingPayoutCents(pendingCents);
            resource.setPaymentsCount(paymentCount != null ? paymentCount : 0L);
            resource.setAvailableNowCents(availableCents);
            resource.setNextPayoutDate(java.time.LocalDate.now().plusDays(7).toString());
            return ResponseEntity.ok(resource);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicles/{vehicleId}/performance")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get per-vehicle performance",
               description = "Returns reservation count, revenue sum, and occupancy percentage for a single vehicle, " +
                             "optionally scoped to a date range. Returns all zeros for a vehicle with no reservation history.")
    public ResponseEntity<VehiclePerformanceResource> getVehiclePerformance(
            @PathVariable @Positive(message = "Vehicle ID must be positive") Long vehicleId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        log.info("GET vehicle performance requested for vehicleId={}, from={}, to={}", vehicleId, from, to);
        try {
            LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
            LocalDate toDate = (to != null && !to.isBlank()) ? LocalDate.parse(to) : null;

            VehiclePerformanceResource resource = vehiclePerformanceService.getPerformance(vehicleId, fromDate, toDate);
            return ResponseEntity.ok(resource);
        } catch (DateTimeParseException ex) {
            log.error("Invalid date format for vehicle performance request, vehicleId={}", vehicleId, ex);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/owners/{ownerId}/earnings/movements")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get owner earnings movements", description = "Returns itemized payment records for an owner within an optional date range (US47/TS12).")
    public ResponseEntity<List<EarningsMovementResource>> getOwnerEarningsMovements(
            @PathVariable @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (!currentUserService.isOwnerOrAdmin(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            LocalDateTime fromDate = (from != null && !from.isBlank())
                    ? LocalDate.parse(from).atStartOfDay()
                    : LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime toDate = (to != null && !to.isBlank())
                    ? LocalDate.parse(to).atTime(23, 59, 59)
                    : LocalDateTime.now();
            if (fromDate.isAfter(toDate)) {
                return ResponseEntity.badRequest().build();
            }

            var movements = paymentsService.findAllByOwnerBetween(ownerId, fromDate, toDate).stream()
                    .map(p -> {
                        var m = new EarningsMovementResource();
                        m.setReservationId(p.getReservationId());
                        m.setAmountCents(p.getAmountCents());
                        m.setStatus(p.getStatus());
                        m.setCreatedAt(p.getCreatedAt() == null ? null : p.getCreatedAt().toString());
                        return m;
                    })
                    .toList();
            return ResponseEntity.ok(movements);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/owners/{ownerId}/withdrawals")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Request withdrawal", description = "Requests a payout withdrawal for an owner against their currently available balance (US48/US49).")
    public ResponseEntity<WithdrawalResource> requestWithdrawal(
            @PathVariable @Positive(message = "Owner ID must be positive") Long ownerId,
            @Valid @RequestBody CreateWithdrawalRequest request) {
        if (!currentUserService.isOwnerOrAdmin(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            var withdrawal = withdrawalService.requestWithdrawal(ownerId, request.getAmountCents(), request.getPayoutDestinationNote());
            var resource = toWithdrawalResource(withdrawal);
            return ResponseEntity.created(URI.create("/api/v1/payments/owners/" + ownerId + "/withdrawals/" + withdrawal.getId()))
                    .body(resource);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/owners/{ownerId}/withdrawals")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get withdrawal history", description = "Returns the owner's paginated withdrawal request history (US49). Page starts at 1.")
    public ResponseEntity<PagedResponse<WithdrawalResource>> getWithdrawalHistory(
            @PathVariable @Positive(message = "Owner ID must be positive") Long ownerId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size) {
        if (!currentUserService.isOwnerOrAdmin(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var results = withdrawalService.getWithdrawalHistory(ownerId, page - 1, size);
        var content = results.getContent().stream().map(this::toWithdrawalResource).toList();
        return ResponseEntity.ok(new PagedResponse<>(content, page, size, results.getTotalElements(), results.getTotalPages()));
    }

    private WithdrawalResource toWithdrawalResource(app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Withdrawal withdrawal) {
        var resource = new WithdrawalResource();
        resource.setId(withdrawal.getId());
        resource.setOwnerId(withdrawal.getOwnerId());
        resource.setAmountCents(withdrawal.getAmountCents());
        resource.setPayoutDestinationNote(withdrawal.getPayoutDestinationNote());
        resource.setStatus(withdrawal.getStatus() == null ? null : withdrawal.getStatus().name());
        resource.setRequestedAt(withdrawal.getRequestedAt() == null ? null : withdrawal.getRequestedAt().toString());
        return resource;
    }
}
