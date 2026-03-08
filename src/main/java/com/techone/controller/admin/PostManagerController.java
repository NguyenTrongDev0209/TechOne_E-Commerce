package com.techone.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.domain.post.entity.Post;
import com.techone.domain.post.repository.PostRepository;

@Controller
public class PostManagerController {
    
    @Autowired
    private PostRepository postRepository;

    @GetMapping("/admin/post-list")
    public String postList(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "dateFrom", required = false) String dateFromStr,
            @RequestParam(value = "dateTo", required = false) String dateToStr,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        Boolean status = null;
        if ("published".equals(statusStr)) status = true;
        else if ("pending".equals(statusStr)) status = false;

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.time.LocalDate dateFrom = null;
        if (dateFromStr != null && !dateFromStr.trim().isEmpty()) {
            try { dateFrom = java.time.LocalDate.parse(dateFromStr.trim(), formatter); } catch(Exception e) {}
        }
        
        java.time.LocalDate dateTo = null;
        if (dateToStr != null && !dateToStr.trim().isEmpty()) {
            try { dateTo = java.time.LocalDate.parse(dateToStr.trim(), formatter); } catch(Exception e) {}
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findByFilters(
            (title != null && !title.trim().isEmpty()) ? title.trim() : null,
            status,
            null, // categoryId
            dateFrom,
            dateTo,
            pageable
        );

        model.addAttribute("posts", postsPage);
        model.addAttribute("paramTitle", title);
        model.addAttribute("paramStatus", statusStr == null ? "all" : statusStr);
        model.addAttribute("paramDateFrom", dateFromStr);
        model.addAttribute("paramDateTo", dateToStr);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        // Stats for the header card
        model.addAttribute("totalPosts", postRepository.count());
        model.addAttribute("hiddenPosts", postRepository.countByStatus(false));
        model.addAttribute("totalViews", postRepository.sumViewCount());
        
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

