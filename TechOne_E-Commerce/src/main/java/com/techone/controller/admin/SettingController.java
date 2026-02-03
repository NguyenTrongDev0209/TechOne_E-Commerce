package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingController {
	
	@GetMapping("/admin/setting")
	public String shippingConfiguration() {
		return "views/admin/setting";
	}
	
}
