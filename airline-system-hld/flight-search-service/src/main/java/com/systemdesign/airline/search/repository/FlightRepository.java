package com.systemdesign.airline.search.repository;

import com.systemdesign.airline.search.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f WHERE f.departureCity = :departureCity AND f.arrivalCity = :arrivalCity " +
           "AND f.departureTime >= :startTime AND f.departureTime <= :endTime AND f.availableSeats > 0")
    List<Flight> searchAvailableFlights(
            @Param("departureCity") String departureCity,
            @Param("arrivalCity") String arrivalCity,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
