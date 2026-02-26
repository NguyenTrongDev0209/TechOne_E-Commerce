package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
}
