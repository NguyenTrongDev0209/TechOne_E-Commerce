package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
	@GetMapping("/client/search")
	public String search() {
		return "client/search";
	}
}
