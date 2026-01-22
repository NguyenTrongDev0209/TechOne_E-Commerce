package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostDetailController {
	@GetMapping("/client/post_detail")
	public String postDetail() {
		return "client/post_detail";
	}
}
