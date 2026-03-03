package com.techone.repository;

import com.techone.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findAllByOrderByCreateAtDesc();

    List<Order> findByAccountIdOrderByCreateAtDesc(Integer accountId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "orderDetail", "orderDetail.variant",
            "orderDetail.variant.product" })
    java.util.Optional<Order> findByOrderCode(Long orderCode);

    Optional<Order> findFirstByAccountIdAndStatus(Integer accountId, Integer status);

    long countByAccountId(Integer accountId);
}
