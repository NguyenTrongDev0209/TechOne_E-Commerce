package com.techone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.product.entity.SpecificationTitle;

@Repository
public interface SpecificationTitleRepository extends JpaRepository<SpecificationTitle, Integer> {
}
