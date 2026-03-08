package com.techone.domain.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.techone.domain.product.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    @Query("SELECT b FROM Brand b WHERE (:keyword IS NULL OR b.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Brand> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByName(String name);

    java.util.List<Brand> findByStatus(Boolean status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
    int countProducts(@Param("brandId") Integer brandId);

    @Query("SELECT new com.techone.dto.BrandCountDto(b, COUNT(p)) FROM Brand b JOIN b.product p WHERE b.status = true AND p.status = true GROUP BY b.id, b.name, b.logo, b.status")
    java.util.List<com.techone.dto.BrandCountDto> findActiveBrandsWithProductCount();
}
