package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportListController {
	
	@GetMapping("/admin/report-list")
	public String shippingConfiguration() {
		return "admin/report-list";
	}
	
}
