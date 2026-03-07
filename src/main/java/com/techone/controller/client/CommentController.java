package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techone.model.Account;
import com.techone.model.Comment;
import com.techone.model.Post;
import com.techone.repository.CommentRepository;
import com.techone.repository.PostRepository;
import com.techone.utils.SessionUtils;

import java.time.LocalDateTime;

@Controller
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @PostMapping("/comments/post")
    public String postComment(@RequestParam("postId") Integer postId,
                              @RequestParam("content") String content,
                              @RequestParam(value = "parentId", required = false) Integer parentId,
                              RedirectAttributes ra) {
        
        Account user = SessionUtils.get("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để bình luận");
            return "redirect:/login";
        }

        if (content == null || content.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Nội dung bình luận không được để trống");
            return "redirect:/posts/" + postId;
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            ra.addFlashAttribute("error", "Bài viết không tồn tại");
            return "redirect:/blogs";
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAccount(user);
        comment.setCreateAt(LocalDateTime.now());
        comment.setStatus(true);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId).orElse(null);
            if (parent != null) {
                comment.setParent(parent);
            }
        }

        commentRepository.save(comment);

        ra.addFlashAttribute("success", "Bình luận thành công");
        return "redirect:/posts/" + postId;
    }
}
