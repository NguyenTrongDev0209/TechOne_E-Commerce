package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.product.entity.Specification;
import com.techone.domain.product.entity.Product;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, Integer> {
    java.util.List<Specification> findByProduct(Product product);
}
