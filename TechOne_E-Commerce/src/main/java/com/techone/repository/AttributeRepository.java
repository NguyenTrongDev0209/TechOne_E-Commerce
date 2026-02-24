package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Integer> {
}
