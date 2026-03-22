package com.systemdesign.airline.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentListenerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    
    // In-memory idempotency store (In prod: Redis or DB table)
    private final ConcurrentHashMap<String, Boolean> processedPayments = new ConcurrentHashMap<>();

    private static final String BOOKING_TOPIC = "booking.reserved";
    private static final String PAYMENT_SUCCESS_TOPIC = "booking.payment.success";
    private static final String PAYMENT_FAIL_TOPIC = "booking.payment.failed";

    @KafkaListener(topics = BOOKING_TOPIC, groupId = "payment-group")
    public void handleBookingReserved(ConsumerRecord<String, String> record) {
        String bookingId = record.key();
        log.info("Received payment request for booking: {}", bookingId);

        // Idempotency check: Have we processed this booking ID already? (e.g. at-least-once delivery)
        if (processedPayments.putIfAbsent(bookingId, true) != null) {
            log.warn("Payment for booking {} already processed. Ignoring.", bookingId);
            return;
        }

        try {
            // Mock External Payment Gateway Call (Stripe/PayPal)
            Thread.sleep(1000); // 1 sec latency

            // Simulate 95% success, 5% failure
            if (Math.random() > 0.05) {
                log.info("Payment SUCCESS for booking {}", bookingId);
                kafkaTemplate.send(PAYMENT_SUCCESS_TOPIC, bookingId, "SUCCESS");
            } else {
                log.error("Payment FAILED for booking {}", bookingId);
                kafkaTemplate.send(PAYMENT_FAIL_TOPIC, bookingId, "INSUFFICIENT_FUNDS");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
