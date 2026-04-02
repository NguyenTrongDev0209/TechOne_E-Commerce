package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.VariantAttributeValue;

public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValue, Integer> {
    java.util.List<VariantAttributeValue> findByVariant(com.techone.model.Variant variant);
}
