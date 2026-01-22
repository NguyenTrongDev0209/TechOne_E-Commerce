package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForgotPasswordController {
	@GetMapping("/client/forgot_password")
	public String form() {
		return "client/forgot_password";
	}
}
