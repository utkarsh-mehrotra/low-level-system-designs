package com.systemdesign.airline.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingReference;
    private Long flightId;
    private Long seatId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // For payment timeout

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, FAILED
    }
}
