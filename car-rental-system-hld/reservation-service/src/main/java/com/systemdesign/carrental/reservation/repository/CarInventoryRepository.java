package com.systemdesign.carrental.reservation.repository;

import com.systemdesign.carrental.reservation.entity.CarInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarInventoryRepository extends JpaRepository<CarInventory, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CarInventory c WHERE c.carId = :carId")
    Optional<CarInventory> findByCarIdForUpdate(@Param("carId") Long carId);
}
