package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChangePasswordController {
	@GetMapping("/account/change-password")
	public String changePassword() {
		return "views/client/change-password";
	}
}
