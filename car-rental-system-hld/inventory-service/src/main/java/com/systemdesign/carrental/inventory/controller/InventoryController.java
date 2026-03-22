package com.systemdesign.carrental.inventory.controller;

import com.systemdesign.carrental.inventory.entity.Car;
import com.systemdesign.carrental.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<Car>> searchCars(
            @RequestParam("location") String location,
            @RequestParam(value = "make", required = false) String make,
            @RequestParam(value = "model", required = false) String model) {
        
        List<Car> cars = inventoryService.searchCars(location, make, model);
        return ResponseEntity.ok(cars);
    }
}
