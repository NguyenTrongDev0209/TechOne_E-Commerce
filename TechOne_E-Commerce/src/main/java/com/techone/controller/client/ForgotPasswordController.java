package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForgotPasswordController {
	
	@GetMapping("/forgot-password")
	public String form() {
		return "fragments/client/forgot-password";
	}
	
}
