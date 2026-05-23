package app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    Optional<Payment> findByReservationId(Long reservationId);
}
