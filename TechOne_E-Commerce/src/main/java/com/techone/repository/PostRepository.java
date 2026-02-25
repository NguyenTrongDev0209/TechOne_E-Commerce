package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
}
