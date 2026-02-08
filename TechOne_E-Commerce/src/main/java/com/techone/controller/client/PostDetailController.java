package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostDetailController {
	
	@GetMapping("/posts/post-detail")
	public String showPostDetail() {
		return "views/client/post-detail";
	}
}
