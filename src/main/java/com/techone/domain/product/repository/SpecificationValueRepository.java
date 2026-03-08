package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.product.entity.SpecificationValue;

@Repository
public interface SpecificationValueRepository extends JpaRepository<SpecificationValue, Integer> {
}
