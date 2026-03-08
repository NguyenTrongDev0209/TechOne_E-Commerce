package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.product.entity.AttributeValue;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Integer> {
}
