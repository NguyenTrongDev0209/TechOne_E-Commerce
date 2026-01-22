package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {
	@GetMapping("/client/contact")
	public String contact() {
		return "client/contact";
	}
}
