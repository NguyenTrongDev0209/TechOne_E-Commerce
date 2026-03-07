package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostsController {

    @Autowired
    private com.techone.repository.PostRepository postRepository;

    @Autowired
    private com.techone.repository.CategoryRepository categoryRepository;

    @GetMapping("/posts")
    public String showPosts(org.springframework.ui.Model model,
                            @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
                            @org.springframework.web.bind.annotation.RequestParam(value = "searchTitle", required = false) String searchTitle,
                            @org.springframework.web.bind.annotation.RequestParam(value = "categoryId", required = false) Integer categoryId) {
        
        if (searchTitle != null && searchTitle.trim().isEmpty()) {
            searchTitle = null;
        }
        
        int pageSize = 7;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
        
        org.springframework.data.domain.Page<com.techone.model.Post> postPage = postRepository.findByFilters(
            searchTitle != null ? searchTitle.trim() : null, 
            true, 
            categoryId, 
            null, 
            null, 
            pageable
        );
        
        model.addAttribute("postPage", postPage);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("searchTitle", searchTitle);
        model.addAttribute("currentCategoryId", categoryId);
        
        // Fetch top 4 categories with most posts
        java.util.List<com.techone.model.Category> topCategories = categoryRepository.findTop4ByPostCount();
        model.addAttribute("topCategories", topCategories);

        // Fetch top 2 trending posts
        java.util.List<com.techone.model.Post> trending = postRepository.findTop2ByStatusOrderByViewCountDesc(true);
        model.addAttribute("trending", trending);

        return "views/client/posts";
    }
}
