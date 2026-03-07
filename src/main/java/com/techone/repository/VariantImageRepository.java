package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.VariantImage;

public interface VariantImageRepository extends JpaRepository<VariantImage, Integer> {
    java.util.List<VariantImage> findByVariant(com.techone.model.Variant variant);
}
