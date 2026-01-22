package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {
	@GetMapping("/client/profile")
	public String profile() {
		return "client/profile";
	}
}
