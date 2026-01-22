package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegisterController {
	@GetMapping("/client/register")
	public String registerForm() {
		return "client/register";
	}
}
