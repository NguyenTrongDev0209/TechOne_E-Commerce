package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoryController {
	
	@GetMapping("/categories")
	public String showCategories() {
		return "fragments/client/categories";
	}
}
