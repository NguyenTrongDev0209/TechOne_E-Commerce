package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.product.entity.ImageProduct;
import java.util.List;

public interface ImageProductRepository extends JpaRepository<ImageProduct, Integer> {
    List<ImageProduct> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);
}
