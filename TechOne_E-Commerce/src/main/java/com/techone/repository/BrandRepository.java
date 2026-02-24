package com.techone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techone.model.Brand;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    @Query("SELECT b FROM Brand b WHERE (:keyword IS NULL OR b.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Brand> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByName(String name);

    java.util.List<Brand> findByStatus(Boolean status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
    int countProducts(@Param("brandId") Integer brandId);
}
