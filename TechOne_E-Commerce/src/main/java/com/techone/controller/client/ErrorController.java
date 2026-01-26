package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
	
	@GetMapping("/error-page")
	public String showError() {
		return "fragments/client/error";
	}
}
