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
    public String postList(
            @org.springframework.web.bind.annotation.RequestParam(value = "title", required = false) String title,
            @org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String statusStr,
            @org.springframework.web.bind.annotation.RequestParam(value = "dateFrom", required = false) String dateFromStr,
            @org.springframework.web.bind.annotation.RequestParam(value = "dateTo", required = false) String dateToStr,
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

        List<Post> posts = postRepository.findByFilters(
            (title != null && !title.trim().isEmpty()) ? title.trim() : null,
            status,
            dateFrom,
            dateTo
        );

        model.addAttribute("posts", posts);
        model.addAttribute("paramTitle", title);
        model.addAttribute("paramStatus", statusStr == null ? "all" : statusStr);
        model.addAttribute("paramDateFrom", dateFromStr);
        model.addAttribute("paramDateTo", dateToStr);
        
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
