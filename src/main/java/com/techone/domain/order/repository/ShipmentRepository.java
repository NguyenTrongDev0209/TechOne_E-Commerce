package com.techone.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.order.entity.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
}
