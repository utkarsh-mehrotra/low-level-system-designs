package com.systemdesign.parking.gate.controller;

import com.systemdesign.parking.gate.entity.ParkingTicket;
import com.systemdesign.parking.gate.service.GateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gate")
@RequiredArgsConstructor
public class GateController {

    private final GateService gateService;

    @PostMapping("/entry")
    public ResponseEntity<?> vehicleEntry(@RequestParam("licensePlate") String licensePlate) {
        try {
            ParkingTicket ticket = gateService.handleVehicleEntry(licensePlate);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(409).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/exit")
    public ResponseEntity<?> vehicleExit(@RequestParam("ticketId") String ticketId) {
        try {
            gateService.handleVehicleExit(ticketId);
            return ResponseEntity.accepted().body("Exit intent registered. Proceeding to payment...");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
