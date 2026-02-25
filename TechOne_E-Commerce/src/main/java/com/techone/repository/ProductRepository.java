package com.techone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.techone.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

        @Query("SELECT p FROM Product p WHERE " +
                        "(:keyword IS NULL OR p.name LIKE %:keyword%) AND " +
                        "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                        "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
                        "(:status IS NULL OR p.status = :status) AND " +
                        "(:fromDate IS NULL OR p.createAt >= :fromDate) AND " +
                        "(:toDate IS NULL OR p.createAt <= :toDate)")
        Page<Product> search(@Param("keyword") String keyword,
                        @Param("categoryId") Integer categoryId,
                        @Param("brandId") Integer brandId,
                        @Param("status") Boolean status,
                        @Param("fromDate") java.time.LocalDateTime fromDate,
                        @Param("toDate") java.time.LocalDateTime toDate,
                        Pageable pageable);

        java.util.List<Product> findByStatus(Boolean status);

        java.util.Optional<Product> findBySlug(String slug);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @Query("UPDATE Product p SET p.status = :status WHERE p.category.id = :categoryId")
        void updateStatusByCategoryId(@Param("categoryId") Integer categoryId, @Param("status") Boolean status);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @Query("UPDATE Product p SET p.status = :status WHERE p.brand.id = :brandId")
        void updateStatusByBrandId(@Param("brandId") Integer brandId, @Param("status") Boolean status);

        @Query("SELECT p FROM Product p WHERE p.status = true AND p.id != :excludeId AND p.category.parent.id = :parentId")
        java.util.List<Product> findSimilarByParentCategory(@Param("parentId") Integer parentId,
                        @Param("excludeId") Integer excludeId, org.springframework.data.domain.Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.status = true AND p.id != :excludeId AND p.category.id = :categoryId")
        java.util.List<Product> findSimilarByCategory(@Param("categoryId") Integer categoryId,
                        @Param("excludeId") Integer excludeId, org.springframework.data.domain.Pageable pageable);
}
