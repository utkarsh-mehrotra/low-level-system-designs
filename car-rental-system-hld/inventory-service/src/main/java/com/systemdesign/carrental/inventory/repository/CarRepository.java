package com.systemdesign.carrental.inventory.repository;

import com.systemdesign.carrental.inventory.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByMakeAndModelAndLocationIgnoreCase(String make, String model, String location);
    List<Car> findByLocationIgnoreCase(String location);
}
