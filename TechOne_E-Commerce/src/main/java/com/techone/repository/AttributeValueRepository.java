package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.AttributeValue;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Integer> {
}
