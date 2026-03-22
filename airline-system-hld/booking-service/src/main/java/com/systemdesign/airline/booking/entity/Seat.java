package com.systemdesign.airline.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long flightId;
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    public enum SeatStatus {
        AVAILABLE, PENDING_PAYMENT, BOOKED
    }
}
