package com.systemdesign.airline.booking.repository;

import com.systemdesign.airline.booking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // The key to SDE3 concurrency mapping.
    // SELECT FOR UPDATE prevents two threads from reading the same 'AVAILABLE' state
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.flightId = :flightId AND s.seatNumber = :seatNumber AND s.status = 'AVAILABLE'")
    Optional<Seat> findAvailableSeatForUpdate(@Param("flightId") Long flightId, @Param("seatNumber") String seatNumber);
}
