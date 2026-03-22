package com.systemdesign.carrental.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "car_inventory_locks")
@Getter
@Setter
@NoArgsConstructor
public class CarInventory {

    @Id
    private Long carId; // Unique car reference ID matching Inventory service

    // Version for optimistic locking if needed, or we use pessimistic via repo
}
