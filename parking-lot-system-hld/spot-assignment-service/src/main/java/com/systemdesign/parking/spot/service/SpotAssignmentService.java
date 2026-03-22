package com.systemdesign.parking.spot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotAssignmentService {

    private final StringRedisTemplate redisTemplate;
    
    // Redis key for the Min-Heap Priority Queue
    private static final String AVAILABLE_SPOTS_KEY = "parking:available_spots:level_1";

    /**
     * Bootstraps the Redis ZSET with mock parking spots for demonstration.
     * The score represents the "distance from the elevator".
     */
    @PostConstruct
    public void initializeLot() {
        if (redisTemplate.hasKey(AVAILABLE_SPOTS_KEY)) {
            redisTemplate.delete(AVAILABLE_SPOTS_KEY);
        }
        
        // ZADD key score member
        redisTemplate.opsForZSet().add(AVAILABLE_SPOTS_KEY, "SPOT-001", 10.0); // Closest
        redisTemplate.opsForZSet().add(AVAILABLE_SPOTS_KEY, "SPOT-002", 15.5);
        redisTemplate.opsForZSet().add(AVAILABLE_SPOTS_KEY, "SPOT-003", 5.0);  // New closest!
        redisTemplate.opsForZSet().add(AVAILABLE_SPOTS_KEY, "SPOT-004", 50.0); // Farthest
        
        log.info("Initialized Redis Min-Heap with 4 parking spots.");
    }

    /**
     * SDE3 O(1) Spot Assignment
     * Pops the spot with the absolute minimum distance score atomically.
     */
    public String assignNearestSpot() {
        // ZPOPMIN removes and returns the element with the lowest score in O(log(N))
        ZSetOperations.TypedTuple<String> popped = redisTemplate.opsForZSet().popMin(AVAILABLE_SPOTS_KEY);
        
        if (popped != null) {
            String spotId = popped.getValue();
            Double distance = popped.getScore();
            log.info("Assigned spot {} (Distance: {})", spotId, distance);
            return spotId;
        }
        
        throw new IllegalStateException("Parking Lot is completely full!");
    }

    /**
     * Un-parks a vehicle and returns the spot back to the Min-Heap.
     */
    public void releaseSpot(String spotId, double distanceScore) {
        redisTemplate.opsForZSet().add(AVAILABLE_SPOTS_KEY, spotId, distanceScore);
        log.info("Released spot {} back into the Min-Heap.", spotId);
    }
}
