package com.systemdesign.carrental.reservation.service;

import com.systemdesign.carrental.reservation.entity.CarInventory;
import com.systemdesign.carrental.reservation.entity.Reservation;
import com.systemdesign.carrental.reservation.repository.CarInventoryRepository;
import com.systemdesign.carrental.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CarInventoryRepository carInventoryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String RESERVED_TOPIC = "car.reserved";

    @Transactional
    public Reservation bookCar(Long carId, Long userId, LocalDate startDate, LocalDate endDate) {
        
        // 1. Acquire pessimistic write lock on the Car row to serialize requests for this specific car
        CarInventory inventoryLock = carInventoryRepository.findByCarIdForUpdate(carId)
                .orElseGet(() -> {
                    // Lazy initialize lock row if it doesn't exist
                    CarInventory newLock = new CarInventory();
                    newLock.setCarId(carId);
                    return carInventoryRepository.save(newLock);
                });

        // 2. Overlap check: Are there any existing overlapping reservations?
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(carId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Car is already booked for these dates.");
        }

        // 3. No overlap, create the reservation
        Reservation reservation = new Reservation();
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setCarId(carId);
        reservation.setUserId(userId);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(Reservation.ReservationStatus.PENDING_PAYMENT);

        Reservation savedReservation = reservationRepository.save(reservation);

        // 4. Emit Async Event to Kafka for Payment
        String payload = String.format("{\"reservationId\":\"%s\", \"userId\":%d}", savedReservation.getReservationId(), userId);
        kafkaTemplate.send(RESERVED_TOPIC, savedReservation.getReservationId(), payload);
        log.info("Reservation {} locked and event published to Kafka", savedReservation.getReservationId());

        return savedReservation;
    }
}
