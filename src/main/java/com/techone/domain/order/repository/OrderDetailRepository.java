package com.techone.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.order.entity.OrderDetail;
import com.techone.domain.product.entity.Variant;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByVariant(Variant variant);
}
