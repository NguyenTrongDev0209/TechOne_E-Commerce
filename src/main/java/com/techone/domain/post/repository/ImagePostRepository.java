package com.techone.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.post.entity.ImagePost;

@Repository
public interface ImagePostRepository extends JpaRepository<ImagePost, Integer> {
}
