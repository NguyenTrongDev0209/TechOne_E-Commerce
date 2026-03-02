package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techone.model.Post;
import com.techone.repository.PostRepository;

@Controller
public class PostDetailController {
	
    @Autowired
    private PostRepository postRepository;

	@GetMapping("/posts/{id}")
	public String showPostDetail(Model model,
								@PathVariable("id") Integer id) {
        Post post = postRepository.findByIdAndStatus(id, true).orElse(null);
        if (post == null) {
            return "redirect:/posts?error=PostNotFound";
        }
        
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        model.addAttribute("post", post);
		return "views/client/post-detail";
	}
}
