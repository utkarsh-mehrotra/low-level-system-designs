package com.systemdesign.airline.booking.service;

import com.systemdesign.airline.booking.entity.Booking;
import com.systemdesign.airline.booking.entity.Seat;
import com.systemdesign.airline.booking.repository.BookingRepository;
import com.systemdesign.airline.booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSagaListener {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    @KafkaListener(topics = "booking.payment.success", groupId = "booking-saga-group")
    @Transactional
    public void handlePaymentSuccess(ConsumerRecord<String, String> record) {
        String bookingRef = record.key();
        log.info("Saga callback: Payment SUCCESS for {}. Finalizing booking.", bookingRef);
        
        Booking booking = bookingRepository.findByBookingReference(bookingRef)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingRef));
        
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        Seat seat = seatRepository.findById(booking.getSeatId()).orElseThrow();
        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepository.save(seat);
    }

    @KafkaListener(topics = "booking.payment.failed", groupId = "booking-saga-group")
    @Transactional
    public void handlePaymentFailure(ConsumerRecord<String, String> record) {
        String bookingRef = record.key();
        log.warn("Saga callback: Payment FAILED for {}. Reverting seat lock.", bookingRef);

        Booking booking = bookingRepository.findByBookingReference(bookingRef)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingRef));

        booking.setStatus(Booking.BookingStatus.FAILED);
        bookingRepository.save(booking);

        // Compensating Transaction
        Seat seat = seatRepository.findById(booking.getSeatId()).orElseThrow();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }
}
