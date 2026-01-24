package com.techone.controller.creator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostsManagerController {
	
	@GetMapping("/creator/posts-manager")
	public String showPostList() {
		return "creator/post-list";
	}
}
