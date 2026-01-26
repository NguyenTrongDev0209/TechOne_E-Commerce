package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController {
	
	@GetMapping("/policy")
	public String showPolicy() {
		return "fragments/client/policy";
	}
	
}
