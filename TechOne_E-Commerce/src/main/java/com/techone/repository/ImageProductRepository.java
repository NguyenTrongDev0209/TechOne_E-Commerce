package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.ImageProduct;
import java.util.List;

public interface ImageProductRepository extends JpaRepository<ImageProduct, Integer> {
    List<ImageProduct> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);
}
