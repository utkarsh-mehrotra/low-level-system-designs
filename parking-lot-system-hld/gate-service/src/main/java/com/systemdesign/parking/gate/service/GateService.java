package com.systemdesign.parking.gate.service;

import com.systemdesign.parking.gate.entity.ParkingTicket;
import com.systemdesign.parking.gate.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GateService {

    private final TicketRepository ticketRepository;
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String SPOT_SERVICE_URL = "http://localhost:8081/api/v1/spots/assign";
    private static final String DEPART_TOPIC = "vehicle.departing";

    @Transactional
    public ParkingTicket handleVehicleEntry(String licensePlate) {
        // Prevent double-entry
        ticketRepository.findByLicensePlateAndStatus(licensePlate, ParkingTicket.TicketStatus.ACTIVE)
                .ifPresent(t -> {
                    throw new IllegalStateException("Vehicle is already parked inside.");
                });

        // 1. Fetch nearest spot via internal API call to Redis Spot Assignment Service (O(1))
        ResponseEntity<Map> response = restTemplate.postForEntity(SPOT_SERVICE_URL, null, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to assign a parking spot.");
        }

        String assignedSpot = (String) response.getBody().get("assignedSpot");

        // 2. Persist to Postgres
        ParkingTicket ticket = new ParkingTicket();
        ticket.setLicensePlate(licensePlate);
        ticket.setSpotId(assignedSpot);
        ticket.setEntryTime(LocalDateTime.now());
        ticket.setStatus(ParkingTicket.TicketStatus.ACTIVE);

        ParkingTicket savedTicket = ticketRepository.save(ticket);
        log.info("Vehicle {} arrived. Issuing Ticket {} for Spot {}", licensePlate, savedTicket.getId(), assignedSpot);

        return savedTicket;
    }

    @Transactional
    public void handleVehicleExit(String ticketId) {
        ParkingTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ticket ID"));

        if (ticket.getStatus() != ParkingTicket.TicketStatus.ACTIVE) {
            throw new IllegalStateException("Ticket is not active or payment already processing.");
        }

        // 1. Mark as pending payment
        ticket.setExitTime(LocalDateTime.now());
        ticket.setStatus(ParkingTicket.TicketStatus.PAYMENT_PENDING);
        ticketRepository.save(ticket);

        // 2. Publish async event to Kafka for Billing/Payment
        String payload = String.format("{\"ticketId\":\"%s\", \"licensePlate\":\"%s\", \"spotId\":\"%s\"}", 
                ticket.getId(), ticket.getLicensePlate(), ticket.getSpotId());
        
        kafkaTemplate.send(DEPART_TOPIC, ticket.getId(), payload);
        log.info("Vehicle {} departing. Emitted async billing event for Ticket {}", ticket.getLicensePlate(), ticketId);
    }
}
