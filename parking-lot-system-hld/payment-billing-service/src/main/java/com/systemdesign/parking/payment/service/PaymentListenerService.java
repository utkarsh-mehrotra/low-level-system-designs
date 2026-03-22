package com.systemdesign.parking.payment.service;

import com.systemdesign.parking.payment.entity.PaymentTransaction;
import com.systemdesign.parking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentListenerService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    private static final String SPOT_RELEASE_URL = "http://localhost:8081/api/v1/spots/release?spotId={spotId}&distance={distance}";

    @KafkaListener(topics = "vehicle.departing", groupId = "payment-processor-group")
    public void processDeparture(String message) {
        log.info("Received departure event: {}", message);
        
        String ticketId = extractField(message, "ticketId");
        String spotId = extractField(message, "spotId");

        // 1. Idempotency Check
        if (paymentRepository.findByTicketId(ticketId).isPresent()) {
            log.warn("Ticket {} has already been paid and processed. Ignoring duplicate message.", ticketId);
            return;
        }

        // 2. Process Duration-Based Payment (Simulated)
        double amountDue = calculateSimulatedCharge();
        boolean paymentSuccess = simulateExternalPaymentGateway();

        if (paymentSuccess) {
            // 3. Record Payment
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setTicketId(ticketId);
            transaction.setAmountCharged(amountDue);
            transaction.setProcessedAt(LocalDateTime.now());
            transaction.setStatus("SUCCESS");
            paymentRepository.save(transaction);

            log.info("Payment SUCCESS for Ticket {}. Amount: ${}", ticketId, amountDue);

            // 4. Release Spot back to Redis Min-Heap
            // In a real system, distance would be retrieved from a database. Simulated as 20.0 here.
            restTemplate.postForEntity(SPOT_RELEASE_URL, null, String.class, spotId, 20.0);
            log.info("Successfully returned Spot {} to the Min-Heap queue. Barrier Opened.", spotId);
        } else {
            log.error("Payment FAILED for Ticket {}. User must provide alternative payment method.", ticketId);
            // Sagas would emit a payment.failed event here that the gate listens to.
        }
    }

    private double calculateSimulatedCharge() {
        return 15.00; // Flat fee for HLD scope
    }

    private boolean simulateExternalPaymentGateway() {
        return true; // 100% success for testing the happy path
    }

    private String extractField(String json, String field) {
        // Quick regex hack parsing
        return json.replaceAll(".*\"" + field + "\":\"([^\"]+)\".*", "$1");
    }
}
