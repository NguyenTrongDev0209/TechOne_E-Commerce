package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostsController {

    @Autowired
    private com.techone.repository.PostRepository postRepository;
	
	@GetMapping("/posts")
	public String showPosts(org.springframework.ui.Model model){
        java.util.List<com.techone.model.Post> posts = postRepository.findByStatusOrderByCreateAtDesc(true);
        model.addAttribute("posts", posts);
        
        // Fetch top 2 trending posts
        java.util.List<com.techone.model.Post> trending = postRepository.findTop2ByStatusOrderByViewCountDesc(true);
        model.addAttribute("trending", trending);
        
		return "views/client/posts";
	}
}
