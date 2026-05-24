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
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;

import java.util.Optional;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    private final ReservationRepository reservationRepository;

    // Inject PaymentsService to update payment records
    private final app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService paymentsService;

    public StripePaymentService(ReservationRepository reservationRepository, app.web.rtgtechnologies.rent2go.payments.application.internal.services.PaymentsService paymentsService) {
        this.reservationRepository = reservationRepository;
        this.paymentsService = paymentsService;
    }

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
                    try {
                        String reservationIdStr = intent.getMetadata().get("reservationId");
                        if (reservationIdStr != null && !reservationIdStr.isBlank()) {
                            Long reservationId = Long.valueOf(reservationIdStr);
                            Optional<app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation> resOpt = reservationRepository.findById(reservationId);
                            if (resOpt.isPresent()) {
                                var reservation = resOpt.get();
                                reservation.markPaid(intent.getId());
                                reservationRepository.save(reservation);
                                log.info("Reservation {} marked as paid (intent={})", reservationId, intent.getId());
                            } else {
                                log.warn("Reservation metadata points to non-existent id: {}", reservationId);
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error handling payment_intent.succeeded webhook: {}", ex.getMessage(), ex);
                    }
                }
            }
            case "charge.refunded" -> log.info("Charge refunded event received: {}", event.getData());
            default -> log.info("Unhandled event type: {}", event.getType());
        }
    }

    public String refundPayment(String paymentIntentId, Long amountCents) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        com.stripe.param.RefundCreateParams.Builder b = com.stripe.param.RefundCreateParams.builder();
        if (amountCents != null) b.setAmount(amountCents);
        b.setPaymentIntent(paymentIntentId);
        com.stripe.model.Refund r = com.stripe.model.Refund.create(b.build());
        return r.getId();
    }
}
