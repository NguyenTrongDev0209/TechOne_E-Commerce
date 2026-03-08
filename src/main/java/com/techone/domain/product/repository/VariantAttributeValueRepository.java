package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.product.entity.VariantAttributeValue;
import com.techone.domain.product.entity.Variant;

public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValue, Integer> {
    java.util.List<VariantAttributeValue> findByVariant(Variant variant);
}
