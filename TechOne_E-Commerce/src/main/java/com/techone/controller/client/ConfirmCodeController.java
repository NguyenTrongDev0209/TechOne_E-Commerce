package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfirmCodeController {
	@GetMapping("/client/confirm_code")
	public String form() {
		return "client/confirm_code";
	}
}
