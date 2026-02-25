package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.model.ImagePost;

@Repository
public interface ImagePostRepository extends JpaRepository<ImagePost, Integer> {
}
