package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostsController {
	
	@GetMapping("/posts")
	public String showPosts(){
		return "views/client/posts";
	}
}
