package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.domain.product.entity.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Integer> {
}
