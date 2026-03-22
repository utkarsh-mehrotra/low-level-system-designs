package com.systemdesign.carrental.reservation.repository;

import com.systemdesign.carrental.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationId(String reservationId);

    // Finds any reservation that overlaps with the requested dates for the same car
    @Query("SELECT r FROM Reservation r WHERE r.carId = :carId AND r.status IN ('PENDING_PAYMENT', 'CONFIRMED') " +
           "AND ((r.startDate <= :reqEnd AND r.endDate >= :reqStart))")
    List<Reservation> findOverlappingReservations(@Param("carId") Long carId, 
                                                  @Param("reqStart") LocalDate reqStart, 
                                                  @Param("reqEnd") LocalDate reqEnd);
}
