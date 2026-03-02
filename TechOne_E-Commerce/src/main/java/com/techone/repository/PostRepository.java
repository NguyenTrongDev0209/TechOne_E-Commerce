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
}
