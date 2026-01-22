package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostListController {
	@GetMapping("/client/post_list")
	public String postList(){
		return "client/post_list";
	}
}
