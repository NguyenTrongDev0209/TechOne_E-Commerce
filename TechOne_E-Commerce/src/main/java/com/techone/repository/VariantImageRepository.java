package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techone.model.VariantImage;

public interface VariantImageRepository extends JpaRepository<VariantImage, Integer> {
}
