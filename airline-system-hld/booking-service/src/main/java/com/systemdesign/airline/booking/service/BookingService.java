package com.systemdesign.airline.booking.service;

import com.systemdesign.airline.booking.entity.Booking;
import com.systemdesign.airline.booking.entity.Seat;
import com.systemdesign.airline.booking.repository.BookingRepository;
import com.systemdesign.airline.booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String BOOKING_TOPIC = "booking.reserved";

    @Transactional
    public Booking reserveSeat(Long flightId, String seatNumber, Long userId) {
        // Core SDE3 Feature: Pessimistic Locking
        log.info("Attempting to lock seat {} for flight {}", seatNumber, flightId);
        Seat seat = seatRepository.findAvailableSeatForUpdate(flightId, seatNumber)
                .orElseThrow(() -> new IllegalStateException("Seat is already booked or currently locked by another transaction."));

        // If we acquired the lock, we mark it pending payment
        seat.setStatus(Seat.SeatStatus.PENDING_PAYMENT);
        seatRepository.save(seat);

        // Create booking record
        Booking booking = new Booking();
        booking.setBookingReference(UUID.randomUUID().toString());
        booking.setFlightId(flightId);
        booking.setSeatId(seat.getId());
        booking.setUserId(userId);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // 10 min to pay

        Booking savedBooking = bookingRepository.save(booking);

        // Emit async event for Payment Service to pick up
        String eventPayload = String.format("{\"bookingId\":\"%s\", \"userId\":%d, \"amount\": 150.0}",
                savedBooking.getBookingReference(), userId);
        
        kafkaTemplate.send(BOOKING_TOPIC, savedBooking.getBookingReference(), eventPayload);
        log.info("Booking {} reserved and event published to Kafka", savedBooking.getBookingReference());

        return savedBooking;
    }
}
