package com.techone.controller.creator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostFormController {
	
	@GetMapping("/creator/posts-manager/post-form")
	public String showPostForm() {
		return "creator/post-form";
	}
}
