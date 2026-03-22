package com.systemdesign.carrental.reservation.service;

import com.systemdesign.carrental.reservation.entity.Reservation;
import com.systemdesign.carrental.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationSagaListener {

    private final ReservationRepository reservationRepository;

    @Transactional
    @KafkaListener(topics = "car.payment.success", groupId = "reservation-saga-group")
    public void handlePaymentSuccess(String message) {
        log.info("Received Payment Success event: {}", message);
        String reservationId = extractReservationId(message);
        
        reservationRepository.findByReservationId(reservationId).ifPresent(reservation -> {
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            log.info("Reservation {} successfully CONFIRMED", reservationId);
        });
    }

    @Transactional
    @KafkaListener(topics = "car.payment.failed", groupId = "reservation-saga-group")
    public void handlePaymentFailed(String message) {
        log.info("Received Payment Failed event: {}", message);
        String reservationId = extractReservationId(message);
        
        reservationRepository.findByReservationId(reservationId).ifPresent(reservation -> {
            // Compensating Transaction
            reservation.setStatus(Reservation.ReservationStatus.FAILED);
            reservationRepository.save(reservation);
            log.info("Reservation {} FAILED. Compensating transaction executed, calendar dates released.", reservationId);
        });
    }

    private String extractReservationId(String message) {
        // Quick regex hack for JSON parsing to keep it dependency light
        return message.replaceAll(".*\"reservationId\":\"([^\"]+)\".*", "$1");
    }
}
