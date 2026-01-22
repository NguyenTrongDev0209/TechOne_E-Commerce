package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChangePasswordController {
	@GetMapping("/client/change_password")
	public String changePassword() {
		return "client/change_password";
	}
}
