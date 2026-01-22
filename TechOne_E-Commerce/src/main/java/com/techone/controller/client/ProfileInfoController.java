package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileInfoController {
	@GetMapping("/client/profile_info")
	public String profileInfo() {
		return "client/profile_info";
	}
}
