package com.techone.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techone.model.Post;
import com.techone.repository.PostRepository;

@Controller
public class PostManagerController {
    
    @Autowired
    private PostRepository postRepository;

    @GetMapping("/admin/post-list")
    public String postList(Model model) {
        List<Post> posts = postRepository.findAll();
        // Lấy danh sách post hiển thị gần nhất
        posts.sort((p1, p2) -> {
            if (p1.getCreateAt() == null || p2.getCreateAt() == null) return 0;
            return p2.getCreateAt().compareTo(p1.getCreateAt());
        });
        model.addAttribute("posts", posts);
        return "views/admin/post-list";
    }

    @GetMapping("/admin/post-list/delete/{id}")
    public String deletePost(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            postRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa bài viết thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bài viết: " + e.getMessage());
        }
        return "redirect:/admin/post-list";
    }
}
