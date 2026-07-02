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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
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
        try {
            var map = stripePaymentService.createPaymentIntent(request.getReservationId(), request.getAmountCents(), request.getCurrency());
            // persist payment record
            paymentsService.createRecord(request.getReservationId(), (String) map.get("id"), request.getAmountCents(), request.getCurrency());
            return ResponseEntity.ok(new CreateIntentResponse((String) map.get("clientSecret"), (String) map.get("id")));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Stripe webhook", description = "Receives Stripe events and synchronizes payment status in the backend.")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            var event = stripePaymentService.constructEvent(payload, sigHeader);
            stripePaymentService.handleWebhookEvent(event);
            // mark payment record succeeded when applicable
            if ("payment_intent.succeeded".equals(event.getType())) {
                var pi = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) {
                    paymentsService.markSucceeded(pi.getId());
                }
            }
            return ResponseEntity.ok("received");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid payload");
        }
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
            resource.setAvailablePayoutCents(cents);
            resource.setPendingPayoutCents(0L);
            resource.setPaymentsCount(paymentCount != null ? paymentCount : 0L);
            resource.setAvailableNowCents(cents);
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
}
