package com.systemdesign.airline.search.controller;

import com.systemdesign.airline.search.entity.Flight;
import com.systemdesign.airline.search.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/search")
    public List<Flight> searchFlights(
            @RequestParam("departure") String departure,
            @RequestParam("arrival") String arrival,
            @RequestParam("date") String date) {
        // Example Date format: 2024-03-25
        return flightService.searchFlights(departure, arrival, date);
    }
}
