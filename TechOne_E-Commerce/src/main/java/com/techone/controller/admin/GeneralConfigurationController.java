package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeneralConfigurationController {
	@GetMapping("/admin/general_configuration")
	public String generalConfiguration() {
		return "admin/general_configuration";
	}
}
