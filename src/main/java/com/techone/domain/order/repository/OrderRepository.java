package com.techone.domain.order.repository;

import com.techone.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findAllByOrderByCreateAtDesc();

    List<Order> findByAccountIdOrderByCreateAtDesc(Integer accountId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "orderDetail", "orderDetail.variant",
            "orderDetail.variant.product" })
    Optional<Order> findByOrderCode(Long orderCode);

    Optional<Order> findFirstByAccountIdAndStatus(Integer accountId, Integer status);

    long countByAccountId(Integer accountId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN o.orderDetail od " +
            "LEFT JOIN od.variant v " +
            "LEFT JOIN v.product p " +
            "WHERE o.account.id = :accountId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:search IS NULL OR CAST(o.id AS string) LIKE %:search% OR p.name LIKE %:search%) " +
            "AND (:startDate IS NULL OR o.createAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createAt <= :endDate) " +
            "ORDER BY o.createAt DESC")
    Page<Order> findByFilters(
            @Param("accountId") Integer accountId,
            @Param("status") Integer status,
            @Param("search") String search,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
