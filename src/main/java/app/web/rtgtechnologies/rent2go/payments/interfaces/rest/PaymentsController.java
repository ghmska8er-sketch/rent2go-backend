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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import com.stripe.exception.StripeException;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateIntentRequest;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateIntentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final FareCalculationServiceImpl fareCalculationService;
    private final StripePaymentService stripePaymentService;
    private final app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService paymentsService;
    private final app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService promoService;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @PostMapping("/calculate")
    public ResponseEntity<MoneyResource> calculateFare(@Valid @RequestBody CalculateFareRequest request) {
        if (request == null || request.getBaseAmount() == null || request.getCurrency() == null) {
            return ResponseEntity.badRequest().build();
        }

        Money base = Money.of(request.getBaseAmount(), request.getCurrency());

        BigDecimal subtotal = request.getBaseAmount();

        List<Fee> fees = request.getFees() == null ? List.of() : request.getFees().stream()
                .map(f -> Fee.of(f.getCode(), f.getAmount()))
                .collect(Collectors.toList());

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
    public ResponseEntity<EarningsReportResource> getOwnerEarnings(
            @PathVariable Long ownerId,
            @RequestParam String from,
            @RequestParam String to) {
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
            resource.setTotalAmountCents(totalCents != null ? totalCents : 0L);
            resource.setAvailablePayoutCents(totalCents != null ? totalCents : 0L);
            resource.setPendingPayoutCents(0L);
            resource.setPaymentsCount(paymentCount != null ? paymentCount : 0L);
            return ResponseEntity.ok(resource);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
