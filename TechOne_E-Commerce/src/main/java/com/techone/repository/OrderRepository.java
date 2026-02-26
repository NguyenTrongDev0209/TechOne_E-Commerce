package com.techone.repository;

import com.techone.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findAllByOrderByCreateAtDesc();

    java.util.Optional<Order> findByOrderCode(Long orderCode);
}
