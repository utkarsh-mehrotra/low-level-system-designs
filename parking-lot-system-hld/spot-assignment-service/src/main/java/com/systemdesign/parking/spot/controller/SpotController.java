package com.systemdesign.parking.spot.controller;

import com.systemdesign.parking.spot.service.SpotAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotAssignmentService spotAssignmentService;

    @PostMapping("/assign")
    public ResponseEntity<?> assignSpot() {
        try {
            String spotId = spotAssignmentService.assignNearestSpot();
            return ResponseEntity.ok(Map.of("assignedSpot", spotId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/release")
    public ResponseEntity<?> releaseSpot(@RequestParam("spotId") String spotId, @RequestParam("distance") double distance) {
        spotAssignmentService.releaseSpot(spotId, distance);
        return ResponseEntity.ok(Map.of("message", "Spot released successfully"));
    }
}
