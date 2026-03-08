package com.techone.domain.post.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.post.entity.Comment;
import com.techone.domain.post.entity.Post;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPostAndStatus(Post post, Boolean status);

    List<Comment> findByPostIdAndStatus(Integer postId, Boolean status);
}
