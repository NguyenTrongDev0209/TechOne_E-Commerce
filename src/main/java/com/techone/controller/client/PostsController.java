package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.techone.domain.post.repository.PostRepository;
import com.techone.domain.product.repository.CategoryRepository;
import com.techone.domain.post.entity.Post;
import com.techone.domain.product.entity.Category;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@Controller
public class PostsController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/posts")
    public String showPosts(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "searchTitle", required = false) String searchTitle,
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {

        if (searchTitle != null && searchTitle.trim().isEmpty()) {
            searchTitle = null;
        }

        int pageSize = 7;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Post> postPage = postRepository.findByFilters(

                searchTitle != null ? searchTitle.trim() : null,
                true,
                categoryId,
                null,
                null,
                pageable);

        model.addAttribute("postPage", postPage);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("searchTitle", searchTitle);
        model.addAttribute("currentCategoryId", categoryId);

        // Fetch top 4 categories with most posts
        List<Category> topCategories = categoryRepository.findTop4ByPostCount();

        model.addAttribute("topCategories", topCategories);

        // Fetch top 2 trending posts
        List<Post> trending = postRepository.findTop2ByStatusOrderByViewCountDesc(true);

        model.addAttribute("trending", trending);

        return "views/client/posts";
    }
}
