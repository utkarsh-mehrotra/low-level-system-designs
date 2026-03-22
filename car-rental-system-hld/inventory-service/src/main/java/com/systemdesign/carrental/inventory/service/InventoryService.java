package com.systemdesign.carrental.inventory.service;

import com.systemdesign.carrental.inventory.entity.Car;
import com.systemdesign.carrental.inventory.repository.CarRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final CarRepository carRepository;

    public InventoryService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Cacheable(value = "car_search", key = "#location + '-' + #make + '-' + #model")
    public List<Car> searchCars(String location, String make, String model) {
        if (make == null || model == null) {
            return carRepository.findByLocationIgnoreCase(location);
        }
        return carRepository.findByMakeAndModelAndLocationIgnoreCase(make, model, location);
    }
}
