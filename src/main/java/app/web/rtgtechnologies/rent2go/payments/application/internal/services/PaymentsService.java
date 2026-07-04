package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Payment;
import app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentsService {

    private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);

    private final PaymentRepository paymentRepository;

    public PaymentsService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createRecord(Long reservationId, String paymentIntentId, Long amountCents, String currency) {
        var p = new Payment(reservationId, paymentIntentId, amountCents, currency, "CREATED");
        return paymentRepository.save(p);
    }

    public Optional<Payment> findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    public Optional<Payment> findByPaymentIntentId(String intentId) {
        return paymentRepository.findByPaymentIntentId(intentId);
    }

    public void markSucceeded(String intentId) {
        var opt = paymentRepository.findByPaymentIntentId(intentId);
        if (opt.isEmpty()) {
            log.warn("markSucceeded: no Payment record found for paymentIntentId={} — status left unchanged", intentId);
            return;
        }
        var p = opt.get();
        p.setStatus("SUCCEEDED");
        paymentRepository.save(p);
    }

    public void markRefunded(String intentId) {
        var opt = paymentRepository.findByPaymentIntentId(intentId);
        if (opt.isEmpty()) {
            log.warn("markRefunded: no Payment record found for paymentIntentId={} — status left unchanged", intentId);
            return;
        }
        var p = opt.get();
        p.setStatus("REFUNDED");
        paymentRepository.save(p);
    }

    public Long sumSucceededAmountCentsByOwnerBetween(Long ownerId, java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return paymentRepository.sumSucceededAmountCentsByOwnerBetween(ownerId, from, to);
    }

    public Long countSucceededPaymentsByOwnerBetween(Long ownerId, java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return paymentRepository.countSucceededPaymentsByOwnerBetween(ownerId, from, to);
    }

    public Long sumSucceededAmountCentsByVehicleBetween(Long vehicleId, java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return paymentRepository.sumSucceededAmountCentsByVehicleBetween(vehicleId, from, to);
    }

    public java.util.List<Payment> findAllByOwnerBetween(Long ownerId, java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return paymentRepository.findAllByOwnerBetween(ownerId, from, to);
    }
}
