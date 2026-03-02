package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.model.Post;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findByIdAndStatus(Integer id, Boolean status);
    java.util.List<Post> findByStatusOrderByCreateAtDesc(Boolean status);
    java.util.List<Post> findTop2ByStatusOrderByViewCountDesc(Boolean status);
    
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p WHERE " +
           "(:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(cast(:dateFrom as date) IS NULL OR p.createAt >= :dateFrom) AND " +
           "(cast(:dateTo as date) IS NULL OR p.createAt <= :dateTo) " +
           "ORDER BY p.createAt DESC")
    java.util.List<Post> findByFilters(
            @org.springframework.data.repository.query.Param("title") String title,
            @org.springframework.data.repository.query.Param("status") Boolean status,
            @org.springframework.data.repository.query.Param("dateFrom") java.time.LocalDate dateFrom,
            @org.springframework.data.repository.query.Param("dateTo") java.time.LocalDate dateTo);
}
