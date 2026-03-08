package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.product.entity.Variant;
import com.techone.domain.product.entity.Product;

public interface VariantRepository extends JpaRepository<Variant, Integer> {
    java.util.List<Variant> findByProduct(Product product);
}
