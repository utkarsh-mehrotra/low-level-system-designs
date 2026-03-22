package com.systemdesign.airline.search.service;

import com.systemdesign.airline.search.entity.Flight;
import com.systemdesign.airline.search.repository.FlightRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

    @Cacheable(value = "flights", key = "#departureCity + '-' + #arrivalCity + '-' + #date")
    public List<Flight> searchFlights(String departureCity, String arrivalCity, String date) {
        LocalDateTime startTime = LocalDateTime.parse(date + "T00:00:00");
        LocalDateTime endTime = LocalDateTime.parse(date + "T23:59:59");
        return flightRepository.searchAvailableFlights(departureCity, arrivalCity, startTime, endTime);
    }
}
