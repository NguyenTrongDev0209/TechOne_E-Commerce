package com.techone.controller.creator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardCreatorController {
	
	@GetMapping("/creator/dashboard")
	public String showDashboard() {
		return "creator/dashboard";
	}
}
