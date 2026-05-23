package app.web.rtgtechnologies.rent2go.payments.application.internal.services;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Payment;
import app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentsService {

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
        opt.ifPresent(p -> {
            p.setStatus("SUCCEEDED");
            paymentRepository.save(p);
        });
    }

    public void markRefunded(String intentId) {
        var opt = paymentRepository.findByPaymentIntentId(intentId);
        opt.ifPresent(p -> {
            p.setStatus("REFUNDED");
            paymentRepository.save(p);
        });
    }
}
