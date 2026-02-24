package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.Variant;

public interface VariantRepository extends JpaRepository<Variant, Integer> {
}
