package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.product.entity.VariantImage;
import com.techone.domain.product.entity.Variant;

public interface VariantImageRepository extends JpaRepository<VariantImage, Integer> {
    java.util.List<VariantImage> findByVariant(Variant variant);
}
