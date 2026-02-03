package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerListController {
	
	@GetMapping("/admin/customer-list")
	public String shippingConfiguration() {
		return "views/admin/customer-list";
	}
	
}
