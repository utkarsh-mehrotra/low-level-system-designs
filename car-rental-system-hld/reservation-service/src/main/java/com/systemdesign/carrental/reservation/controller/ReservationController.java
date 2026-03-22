package com.systemdesign.carrental.reservation.controller;

import com.systemdesign.carrental.reservation.entity.Reservation;
import com.systemdesign.carrental.reservation.service.ReservationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/book")
    public ResponseEntity<?> bookCar(@RequestBody BookingRequest request) {
        try {
            Reservation reservation = reservationService.bookCar(
                    request.getCarId(),
                    request.getUserId(),
                    request.getStartDate(),
                    request.getEndDate()
            );
            return ResponseEntity.ok(reservation);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage());
        }
    }

    @Data
    static class BookingRequest {
        private Long carId;
        private Long userId;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
