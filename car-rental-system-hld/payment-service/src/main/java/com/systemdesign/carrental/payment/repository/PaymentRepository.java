package com.systemdesign.carrental.payment.repository;

import com.systemdesign.carrental.payment.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByReservationId(String reservationId);
}
