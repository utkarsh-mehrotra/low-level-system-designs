package com.systemdesign.parking.gate.repository;

import com.systemdesign.parking.gate.entity.ParkingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<ParkingTicket, String> {
    Optional<ParkingTicket> findByLicensePlateAndStatus(String licensePlate, ParkingTicket.TicketStatus status);
}
