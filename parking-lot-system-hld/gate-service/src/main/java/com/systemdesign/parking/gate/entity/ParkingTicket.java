package com.systemdesign.parking.gate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_tickets")
@Getter
@Setter
@NoArgsConstructor
public class ParkingTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String spotId;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    public enum TicketStatus {
        ACTIVE,
        PAYMENT_PENDING,
        PAID
    }
}
