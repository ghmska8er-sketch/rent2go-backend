package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    public Map<String, Object> createPaymentIntent(Long reservationId, Long amountCents, String currency) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(currency)
                .putMetadata("reservationId", String.valueOf(reservationId))
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        Map<String, Object> resp = new HashMap<>();
        resp.put("clientSecret", intent.getClientSecret());
        resp.put("id", intent.getId());
        return resp;
    }

    public Event constructEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
    }

    public void handleWebhookEvent(Event event) {
        log.info("Received Stripe event: {}", event.getType());
        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (intent != null) {
                    log.info("PaymentIntent succeeded: {} metadata: {}", intent.getId(), intent.getMetadata());
                }
            }
            case "charge.refunded" -> log.info("Charge refunded event received: {}", event.getData());
            default -> log.info("Unhandled event type: {}", event.getType());
        }
    }
}
