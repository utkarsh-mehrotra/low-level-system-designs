package com.systemdesign.parking.payment.repository;

import com.systemdesign.parking.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTicketId(String ticketId);
}
