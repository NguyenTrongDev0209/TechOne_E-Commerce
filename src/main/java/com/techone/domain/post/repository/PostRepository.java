package com.techone.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.techone.domain.post.entity.Post;
import com.techone.domain.product.entity.Category;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findByIdAndStatus(Integer id, Boolean status);

    java.util.List<Post> findByStatusOrderByCreateAtDesc(Boolean status);

    java.util.List<Post> findTop2ByStatusOrderByViewCountDesc(Boolean status);

    java.util.List<Post> findTop3ByStatusOrderByCreateAtDesc(Boolean status);

    java.util.List<Post> findTop5ByStatusOrderByViewCountDesc(Boolean status);

    java.util.List<Post> findTop3ByCategoryAndIdNotAndStatusOrderByViewCountDesc(Category category, Integer id,
            Boolean status);

    @Query("SELECT p FROM Post p WHERE " +
            "(:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(cast(:dateFrom as date) IS NULL OR p.createAt >= :dateFrom) AND " +
            "(cast(:dateTo as date) IS NULL OR p.createAt <= :dateTo) " +
            "ORDER BY p.createAt DESC")
    Page<Post> findByFilters(
            @Param("title") String title,
            @Param("status") Boolean status,
            @Param("categoryId") Integer categoryId,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo,
            Pageable pageable);

    long countByStatus(Boolean status);

    @Query("SELECT SUM(p.viewCount) FROM Post p")
    Long sumViewCount();
}
