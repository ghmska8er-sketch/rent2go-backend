package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.VehicleAvailabilityRepository;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.VehicleAvailability;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.valueobjects.DateRange;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    private final ReservationRepository reservationRepository;
    private final VehicleAvailabilityRepository availabilityRepository;
    private final PaymentsService paymentsService;

    public StripePaymentService(
            ReservationRepository reservationRepository,
            VehicleAvailabilityRepository availabilityRepository,
            PaymentsService paymentsService) {
        this.reservationRepository = reservationRepository;
        this.availabilityRepository = availabilityRepository;
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

    private static final long MINIMUM_AMOUNT_CENTS_PEN = 100;

    private void validateCheckoutAmount(Long amountCents, String currency) {
        if (amountCents == null || amountCents <= 0) {
            throw new IllegalArgumentException("Amount in cents must be positive.");
        }
        if ("PEN".equalsIgnoreCase(currency) && amountCents < MINIMUM_AMOUNT_CENTS_PEN) {
            throw new IllegalArgumentException(
                    "Minimum amount for PEN is " + MINIMUM_AMOUNT_CENTS_PEN + " cents (1.00 PEN)."
            );
        }
    }

    public Event constructEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
    }

    @Transactional
    public void handleWebhookEvent(Event event) {
        log.info("Received Stripe event: {}", event.getType());
        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = deserializePaymentIntent(event);
                if (intent != null) {
                    log.info("PaymentIntent succeeded: {} metadata: {}", intent.getId(), intent.getMetadata());
                    handlePaymentSucceeded(intent);
                } else {
                    log.warn("payment_intent.succeeded event (id={}) could not be deserialized to a PaymentIntent " +
                            "even via deserializeUnsafe() fallback — likely a malformed payload. No side effects applied.",
                            event.getId());
                }
            }
            case "charge.refunded" -> log.info("Charge refunded event received: {}", event.getData());
            default -> log.info("Unhandled event type: {}", event.getType());
        }
    }

    /**
     * Deserializes the event's data object to a {@link PaymentIntent}.
     *
     * <p>{@code EventDataObjectDeserializer.getObject()} silently returns {@link Optional#empty()}
     * (no exception) whenever the event's embedded {@code api_version} does not match the
     * {@code stripe-java} SDK version this service is compiled against — a common real-world
     * mismatch when the Stripe account/webhook is configured with a newer API version than the
     * pinned SDK. Previously, both the reservation-confirm path (here) and the payment-record
     * status update (formerly a second, independent {@code getObject()} call in
     * {@code PaymentsController.webhook()}) relied on this fragile call, so a version mismatch
     * would silently no-op status updates. Stripe's own docs recommend {@code deserializeUnsafe()}
     * as the fallback for exactly this case, since it deserializes directly from the raw JSON
     * without the version-matching guard.
     */
    private PaymentIntent deserializePaymentIntent(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        var obj = deserializer.getObject();
        if (obj.isPresent()) {
            return (PaymentIntent) obj.get();
        }
        try {
            return (PaymentIntent) deserializer.deserializeUnsafe();
        } catch (Exception ex) {
            log.warn("deserializeUnsafe() fallback failed for event {}: {}", event.getId(), ex.getMessage());
            return null;
        }
    }

    private void handlePaymentSucceeded(PaymentIntent intent) {
        try {
            applyPaymentSucceeded(intent);
        } catch (Exception ex) {
            log.error("Error handling payment_intent.succeeded webhook: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Applies the "payment succeeded" side effects (confirm + mark paid + block availability)
     * for a given Stripe PaymentIntent. Shared by the webhook handler (source of truth) and by
     * {@link #syncPaymentIntent(String)}, the defensive fallback used when the webhook has not
     * (yet) reached the backend — see docs/integracion-stripe.md.
     */
    private void applyPaymentSucceeded(PaymentIntent intent) {
        String reservationIdStr = intent.getMetadata().get("reservationId");
        if (reservationIdStr == null || reservationIdStr.isBlank()) {
            log.warn("payment_intent.succeeded received without reservationId in metadata (intentId={})", intent.getId());
            return;
        }

        Long reservationId = Long.valueOf(reservationIdStr);
        Optional<app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation> resOpt =
                reservationRepository.findById(reservationId);

        if (resOpt.isEmpty()) {
            log.warn("Reservation metadata points to non-existent id: {}", reservationId);
            return;
        }

        var reservation = resOpt.get();

        // Mark paid + auto-confirm if PENDING (markPaid already calls confirm() internally)
        if (!reservation.getStatus().isConfirmed() && !reservation.getStatus().isTerminal()) {
            reservation.markPaid(intent.getId());
        } else if (reservation.getStatus().isConfirmed()) {
            // Already confirmed — just record the paymentIntentId if not set
            log.info("Reservation {} already confirmed, skipping re-confirm", reservationId);
        }

        reservationRepository.save(reservation);
        log.info("Reservation {} confirmed and paid (intent={})", reservationId, intent.getId());

        // Auto-block vehicle availability for reservation dates
        blockVehicleDatesForReservation(reservation);

        // Mark the Payment record succeeded in the same place/transaction as the reservation
        // update, using the exact same (safely-deserialized) intent id — eliminates the previous
        // duplicate, independent deserialization that used to live in PaymentsController.webhook().
        
        //paymentsService.markSucceeded(intent.getId());
        var paymentOpt = paymentsService.findByPaymentIntentId(intent.getId());

        if (paymentOpt.isPresent()) {

            paymentsService.markSucceeded(intent.getId());

        } else {

            paymentsService.createRecord(
                    reservationId,
                    intent.getId(),
                    intent.getAmount(),
                    intent.getCurrency().toUpperCase()
            );

            paymentsService.markSucceeded(intent.getId());

            log.info("Payment record created from webhook. reservationId={}, paymentIntentId={}",
                    reservationId,
                    intent.getId());
        }
    }

    /**
     * Defensive fallback for when the Stripe webhook has not reached the backend (e.g. webhook
     * not yet registered in the Stripe Dashboard, or misconfigured signing secret). Retrieves the
     * PaymentIntent directly from Stripe's API and, if it reports "succeeded", applies the exact
     * same confirm/mark-paid/block-availability logic the webhook would have applied. This does
     * NOT replace the webhook as the source of truth — it only lets an already-successful charge
     * unblock a reservation stuck in PENDING when the async notification never arrived.
     *
     * @return true if the reservation was found and the PaymentIntent is "succeeded" (i.e. the
     *         reservation is confirmed/paid as of this call, whether just now or previously).
     */
    @Transactional
    public boolean syncPaymentIntent(String paymentIntentId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        if (!"succeeded".equals(intent.getStatus())) {
            log.info("syncPaymentIntent: intent {} status is '{}', nothing to apply", paymentIntentId, intent.getStatus());
            return false;
        }
        applyPaymentSucceeded(intent);
        return true;
    }

    private void blockVehicleDatesForReservation(
            app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation reservation) {
        try {
            Long vehicleId = reservation.getVehicleId();
            DateRange range = reservation.getDateRange();

            // Check if a block for this reservation already exists to ensure idempotency
            boolean alreadyBlocked = availabilityRepository.findAllByVehicleId(vehicleId).stream()
                    .anyMatch(b -> b.overlaps(range));

            if (!alreadyBlocked) {
                VehicleAvailability block = VehicleAvailability.block(vehicleId, range);
                availabilityRepository.save(block);
                log.info("Blocked vehicle {} from {} to {} after payment confirmed",
                        vehicleId, range.getStartDate(), range.getEndDate());
            } else {
                log.info("Vehicle {} already has an overlapping block for reservation dates — skipping", vehicleId);
            }
        } catch (Exception ex) {
            log.warn("Could not auto-block vehicle availability after payment: {}", ex.getMessage());
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

    public Session createCheckoutSession(
            Long reservationId,
            Long amountCents,
            String currency) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        validateCheckoutAmount(amountCents, currency);

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(
                                "https://rent2go-fe7ed.web.app/#/bookings")
                        .setCancelUrl(
                                "https://rent2go-fe7ed.web.app/#/bookings")
                        .setPaymentIntentData(
                                SessionCreateParams.PaymentIntentData.builder()
                                        .putMetadata("reservationId", String.valueOf(reservationId))
                                        .build())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(currency)
                                                        .setUnitAmount(amountCents)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Rent2Go Reservation")
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();

        Session session = Session.create(params);
        if (session.getPaymentIntent() == null && session.getId() != null) {
            log.warn("Checkout session created without payment_intent; retrieving expanded session for id={}", session.getId());
            session = Session.retrieve(
                    session.getId(),
                    SessionRetrieveParams.builder()
                            .addExpand("payment_intent")
                            .build(),
                    null);
        }
        return session;
    }
}
