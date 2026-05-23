package app.web.rtgtechnologies.rent2go.payments.interfaces.rest;

import app.web.rtgtechnologies.rent2go.payments.application.internal.services.FareCalculationServiceImpl;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Discount;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Fee;
import app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Money;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CalculateFareRequest;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.DiscountDto;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.FeeDto;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.MoneyResource;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.stripe.exception.StripeException;
import app.web.rtgtechnologies.rent2go.payments.application.internal.services.StripePaymentService;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateIntentRequest;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreateIntentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;


@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
public class PaymentsController {

    private final FareCalculationServiceImpl fareCalculationService;
    private final StripePaymentService stripePaymentService;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @PostMapping("/calculate")
    public ResponseEntity<MoneyResource> calculateFare(@RequestBody CalculateFareRequest request) {
        if (request == null || request.getBaseAmount() == null || request.getCurrency() == null) {
            return ResponseEntity.badRequest().build();
        }

        Money base = Money.of(request.getBaseAmount(), request.getCurrency());

        List<Fee> fees = request.getFees() == null ? List.of() : request.getFees().stream()
                .map(f -> Fee.of(f.getCode(), f.getAmount()))
                .collect(Collectors.toList());

        List<Discount> discounts = request.getDiscounts() == null ? List.of() : request.getDiscounts().stream()
                .map(d -> Discount.of(d.getCode(), d.getPercentage()))
                .collect(Collectors.toList());

        Money result = fareCalculationService.calculate(base, fees, discounts);
        MoneyResource res = new MoneyResource(result.getAmount(), result.getCurrency());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/create-intent")
    public ResponseEntity<CreateIntentResponse> createIntent(@RequestBody CreateIntentRequest request) {
        try {
            var map = stripePaymentService.createPaymentIntent(request.getReservationId(), request.getAmountCents(), request.getCurrency());
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
            return ResponseEntity.ok("received");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid payload");
        }
    }
}
