package com.techone.controller.authentic;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForgotPasswordController {

	@GetMapping("/forgot-password")
	public String form() {
		return "views/authentic/forgot-password";
	}

}
