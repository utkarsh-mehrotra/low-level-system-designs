package com.systemdesign.airline.booking.controller;

import com.systemdesign.airline.booking.entity.Booking;
import com.systemdesign.airline.booking.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveSeat(@RequestBody ReservationRequest request) {
        try {
            Booking booking = bookingService.reserveSeat(
                    request.getFlightId(), 
                    request.getSeatNumber(), 
                    request.getUserId()
            );
            return ResponseEntity.ok(booking);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage());
        }
    }

    @Data
    static class ReservationRequest {
        private Long flightId;
        private String seatNumber;
        private Long userId;
    }
}
