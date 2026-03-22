package com.systemdesign.carrental.payment.service;

import com.systemdesign.carrental.payment.entity.PaymentRecord;
import com.systemdesign.carrental.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentListenerService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String SUCCESS_TOPIC = "car.payment.success";
    private static final String FAILED_TOPIC = "car.payment.failed";

    @KafkaListener(topics = "car.reserved", groupId = "payment-processor-group")
    public void processPayment(String message) {
        log.info("Received car.reserved event: {}", message);
        
        String reservationId = extractReservationId(message);
        
        // 1. SDE3 Idempotency Check
        if (paymentRepository.findByReservationId(reservationId).isPresent()) {
            log.warn("Payment for reservation {} was already processed! Ignoring duplicate Kafka message.", reservationId);
            return;
        }

        // 2. Simulate Payment Processing (Stripe / PayPal)
        boolean paymentSuccess = simulateExternalPaymentGateway();

        // 3. Record Payment
        PaymentRecord record = new PaymentRecord();
        record.setReservationId(reservationId);
        record.setAmount(100.00); // Fixed deposit amount for simplicity
        record.setProcessedAt(LocalDateTime.now());
        record.setStatus(paymentSuccess ? PaymentRecord.PaymentStatus.SUCCESS : PaymentRecord.PaymentStatus.FAILED);
        
        paymentRepository.save(record);

        // 4. Emit Async Saga Result
        if (paymentSuccess) {
            log.info("Payment SUCCESS for reservation {}", reservationId);
            kafkaTemplate.send(SUCCESS_TOPIC, reservationId, "{\"reservationId\":\"" + reservationId + "\"}");
        } else {
            log.info("Payment FAILED for reservation {}", reservationId);
            kafkaTemplate.send(FAILED_TOPIC, reservationId, "{\"reservationId\":\"" + reservationId + "\"}");
        }
    }

    private boolean simulateExternalPaymentGateway() {
        // 80% success rate
        return Math.random() < 0.8;
    }

    private String extractReservationId(String message) {
        return message.replaceAll(".*\"reservationId\":\"([^\"]+)\".*", "$1");
    }
}
